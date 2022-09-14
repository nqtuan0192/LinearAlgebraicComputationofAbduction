from functools import partial
import re
import numpy as np
import argparse
import copy
import itertools
import time
import math
import sys
import os
from multiprocessing import Process, context
from bitsets import bitset
import pandas as pd

from bidict import bidict
from scipy import sparse

from pysat.examples.hitman import Hitman


def eliminate(items_set):
    if len(items_set) < 1:
        return items_set
    sorted_items_set = sorted(list(set(items_set)), key=lambda x: (len(x), x))
    new_items_set = list()
    min_size = len(sorted_items_set[0])
    for item in sorted_items_set:
        if len(item) > min_size:
            break
        new_items_set.append(item)
    n_added = len(new_items_set)
    good_idx = n_added
    for idx, item in enumerate(sorted_items_set[n_added:]):
        l = len(item)
        if l > min_size:
            min_size = l
            good_idx = len(new_items_set)
        is_good = True
        for correct_set in new_items_set[: good_idx]:
            if item.issuperset(correct_set):
                is_good = False
                break
        if is_good:
            new_items_set.append(item)
    return set(new_items_set)


early_stop_conditions = {'default': 26_000,
                         'phcap_140_5_5_5.atms': 2_00_000, 'phcap_100_4_4_3.atms': 200_000, 'phcap_120_2_2_4.atms': 50_000,
                         'FMEA_12obs_file21.atms': 2_000_000, 'FMEA_12obs_file22.atms': 2_000_000,
                         'FMEA_12obs_file25.atms': 2_000_000, 'FMEA_12obs_file24.atms': 2_000_000,
                         'FMEA_6obs_file28.atms': 2_000_000, 'FMEA_12obs_file10.atms': 2_000_000
                         }

excluding_files = {"phcap_140_5_5_5.atms"
                   }


def readInputData(filename):
    with open(filename, "r") as inpfile:
        hypotheses = set()
        effects = set()
        observations = set()
        facts = set()
        rules = list()
        rawdata = inpfile.readlines()
        for line in rawdata:
            parts = re.split('->', line.strip().replace(".", ""))
            if parts[-1].strip() == 'explain':
                observations.add('explain')
            elif parts[-1].strip() == 'obs':
                observations.add('obs')
            for part in parts:
                words = part.strip().split(',')
                for word in words:
                    if word[0] == 'H':
                        hypotheses.add(word)
                    elif word[0] == 'e':
                        effects.add(word)
            if len(parts) == 1:  # handle facts
                head = [x.strip() for x in parts[0].strip().split(',')]
                facts.add(head[0])
                rules.append(('fact', head, []))
                continue
            head = [x.strip() for x in parts[1].strip().split(',')]
            body = [x.strip() for x in parts[0].strip().split(',')]
            rules.append(('and', head, body))
        return rawdata, hypotheses, effects, observations, rules, facts


def mapAtomsToIndices(atoms_mapping, atom_symbols, requires_type):
    return requires_type(atoms_mapping[sym] for sym in atom_symbols)


def mapAtomsToSymbols(atoms_mapping, atom_indices, requires_type):
    return requires_type(atoms_mapping.inverse[idx] for idx in atom_indices)


def doAtommapping(rules):
    atoms_mapping = bidict()
    atoms_count = 0
    for r in rules:
        for h in r[1]:
            if h not in atoms_mapping:
                atoms_mapping[h] = atoms_count
                atoms_count += 1
        for b in r[2]:
            if b not in atoms_mapping:
                atoms_mapping[b] = atoms_count
                atoms_count += 1
    return atoms_mapping


def standardizeRules(rules, atoms_mapping):
    out_rules = list()
    out_atoms_mapping = copy.deepcopy(atoms_mapping)
    atoms_mapping_count = len(atoms_mapping)

    introduced_atoms_mapping = bidict()

    new_atom_count = 0
    idx = 0
    while idx < len(rules):
        r = rules[idx]
        for count, rn in enumerate(rules[idx:]):
            if r[1] != rn[1]:
                break
        if count == 0:
            idx += 1
            out_rules.append((r[0], r[1], set(r[2])))
        elif count == 1:
            out_rules.append((r[0], r[1], set(r[2])))
        else:
            added_atoms = list()
            unchange_atoms = list()
            for rn in rules[idx:]:
                if r[1] != rn[1]:
                    break
                if len(rn[2]) > 1:
                    body = frozenset(rn[2])
                    if body not in introduced_atoms_mapping.values():
                        out_rules.append(
                            ('and', ['x_' + str(new_atom_count)], set(rn[2])))
                        added_atoms.append('x_' + str(new_atom_count))
                        introduced_atoms_mapping['x_' +
                                                 str(new_atom_count)] = body
                        out_atoms_mapping['x_' +
                                          str(new_atom_count)] = atoms_mapping_count
                        atoms_mapping_count += 1
                        new_atom_count += 1
                    else:
                        added_atoms.append(
                            introduced_atoms_mapping.inverse[body])
                else:
                    unchange_atoms.append(rn[2][0])
            out_rules.append(('or', r[1], set(unchange_atoms + added_atoms)))
        idx += count

    return out_rules, out_atoms_mapping


def mapRulesToIndices(atoms_mapping, rules, requires_type):
    return requires_type((atoms_mapping[head], [atoms_mapping[lit] for lit in body]) for head, body in rules.items())


def mapRulesToSymbols(atoms_mapping, rules, requires_type):
    return requires_type((atoms_mapping.inverse[head], [atoms_mapping.inverse[lit] for lit in body]) for head, body in rules.items())


def buildMatrix(rules, atoms_mapping):
    headatoms = set()
    mp = np.zeros((len(atoms_mapping), len(atoms_mapping)), dtype=float)

    # build matrix program
    for r in rules:
        # find abducible atoms which are undifined (not appear in the head of rules)
        headatoms.add(r[1][0])

        if r[0] == 'and':
            for p in r[2]:
                mp[atoms_mapping[r[1][0]], atoms_mapping[p]] = 1.0 / len(r[2])
        elif r[0] == 'or':
            for p in r[2]:
                mp[atoms_mapping[r[1][0]], atoms_mapping[p]] = 1.0
        elif r[0] == 'fact':
            mp[atoms_mapping[r[1][0]], atoms_mapping[r[1][0]]] = 1.0

    # set abducible atoms which are undifined (not appear in the head of rules)
    for atom in atoms_mapping:
        if atom not in headatoms:
            mp[atoms_mapping[atom], atoms_mapping[atom]] = 1.0

    return mp


def buildMatrix_sparse(rules, atoms_mapping):
    row, col, val = [], [], []
    headatoms = set()

    # build matrix program
    for r in rules:
        # find abducible atoms which are undifined (not appear in the head of rules)
        headatoms.add(r[1][0])

        if r[0] == 'and':
            for p in r[2]:
                row.append(atoms_mapping[r[1][0]])
                col.append(atoms_mapping[p])
                val.append(1.0 / len(r[2]))
        elif r[0] == 'or':
            for p in r[2]:
                row.append(atoms_mapping[r[1][0]])
                col.append(atoms_mapping[p])
                val.append(1.0)
        elif r[0] == 'fact':
            row.append(atoms_mapping[r[1][0]])
            col.append(atoms_mapping[r[1][0]])
            val.append(1.0)

    # set abducible atoms which are undifined (not appear in the head of rules)
    for atom in atoms_mapping:
        if atom not in headatoms:
            row.append(atoms_mapping[atom])
            col.append(atoms_mapping[atom])
            val.append(1.0)

    ms = sparse.coo_matrix((val, (row, col)), shape=(
        len(atoms_mapping), len(atoms_mapping))).tocsr()
    return ms


def solveSetCover(splits_set):
    combinations = itertools.product(*splits_set, repeat=1)
    combinations_reduced = {frozenset(combination)
                            for combination in combinations}
    combinations_reduced = eliminate(combinations_reduced)
    return combinations_reduced


def solveSetCover_improved(items_set):
    hitset = set()
    with Hitman(bootstrap_with=items_set, htype='sorted') as hitman:
        for hs in hitman.enumerate():
            hitset.add(frozenset(hs))
    return hitset


def doSplitMulti(exp, split_indices, splits_set):
    vp_set = list()
    combinations_reduced = solveSetCover(splits_set)
    temp = exp.difference(split_indices)
    for com in combinations_reduced:
        vp_set.append(temp.union(com))
    return set(vp_set)


def doSplitMulti_cached(exp, split_indices, splitted_set):
    vp_set = list()
    temp = exp.difference(split_indices)
    for com in splitted_set:
        vp_set.append(temp.union(com))
    return set(vp_set)


def doSplitMulti_improved(exp, split_indices, splits_set):
    temp = exp.difference(split_indices)
    vp_set = list()
    with Hitman(bootstrap_with=splits_set, htype='sorted') as hitman:
        for hs in hitman.enumerate():
            vp_set.append(temp.union(frozenset(hs)))
    return set(vp_set)


EARLY_STOP = 200_000
LIB_CALL = 50_000


def doAbduction(mp, v, hypotheses_mapped, rules, and_rules_mapped, or_rules_mapped, atoms_mapping, timeout):
    maximum_explanations_size = 0
    final_set = set()
    while True:
        v_new = np.dot(mp.T, v)

        if v.shape[1] > maximum_explanations_size:
            maximum_explanations_size = v.shape[1]
        # check for equality and return
        if np.sum(np.absolute(v - v_new)) == 0:
            return eliminate(final_set), maximum_explanations_size, False
        # check shape is too large and return
        if v.shape[1] > EARLY_STOP:
            print("Early stopped due to", "Matrix size:", v.shape[1])
            return eliminate(final_set), maximum_explanations_size, True

        # asign v by v_new
        v = v_new

        v_list = list()
        # loop over only explainable rules
        for exp_mapped in v[:, np.logical_not(np.sum(v, axis=0) < 0.999)].T:
            exp = frozenset(
                {idx for idx, val in enumerate(exp_mapped) if val > 0})
            v_list.append(exp)

        check_new_split = True
        while check_new_split:
            check_new_split = False
            v_set_new = set()
            for exp in v_list:
                # check final answer
                if exp.issubset(hypotheses_mapped):
                    final_set.add(exp)
                    continue

                # check split
                explainable_or = exp.intersection(or_rules_mapped.keys())
                if bool(explainable_or):
                    # do split with multiple OR-rules
                    splits_set = {
                        frozenset(or_rules_mapped[a]) for a in explainable_or}
                    splits_size = np.prod([len(s) for s in splits_set])
                    # only call MHS backend for large-size set
                    if splits_size > LIB_CALL:
                        print("Calling MHS backend...",
                              "splits size =", splits_size)
                        print("explainable_or", explainable_or)
                        print("explanation", exp)
                        v_set_new.update(doSplitMulti_improved(
                            exp, explainable_or, splits_set))
                    else:
                        v_set_new.update(doSplitMulti(
                            exp, explainable_or, splits_set))
                    check_new_split = True
                    continue
                # just add to another list
                v_set_new.add(exp)

            v_list = list(v_set_new)
        # reproduce explanation matrix in dense format
        v = np.zeros(shape=(len(atoms_mapping), len(v_list)))
        for idx, exp in enumerate(v_list):
            for atom in exp:
                v[atom, idx] = 1.0 / len(exp)


pre_row = np.empty(50_000_000, dtype=np.int32)
pre_col = np.empty(50_000_000, dtype=np.int32)
pre_val = np.empty(50_000_000, dtype=float)
time_accum = 0
maximum_explanations_size = 0
maximum_explanations_nnz = 0
minimum_explanations_sparsity = 1.0
max_iterations = 0


def doAbduction_sparse(ms, v, hypotheses_mapped, facts_mapped, fs, rules, and_rules_mapped, or_rules_mapped, atoms_mapping, timeout):
    final_set = set()
    matrix_size = len(atoms_mapping)
    MHS_CACHE = dict()
    global time_accum
    depth = 0

    hit_ornodes = frozenset({})
    while True:
        v_new = sparse.csc_matrix.dot(ms.T, v)

        # check for equality and return
        if np.sum(np.absolute(v - v_new)) == 0:
            return eliminate(final_set), False
        # check shape is too large and return
        if v.shape[1] > EARLY_STOP:
            print("Early stopped due to", "Matrix size:", v.shape[1])
            return eliminate(final_set), True

        # asign v by v_new
        v = v_new

        v_list_orcomputable = list()
        v_list_andcomputable = list()
        v_set_new = set()
        for beg, end in zip(v.indptr[:-1], v.indptr[1:]):
            sum_check = np.sum(v.data[beg: end])
            # check unexplainable rule
            if sum_check < 0.999:
                continue

            exp = frozenset({int(item) for item in v.indices[beg: end]})

            # check final answer
            if exp.issubset(hypotheses_mapped):
                final_set.add(exp.difference(facts_mapped))
                continue

            # check split
            explainable_or = exp.intersection(
                or_rules_mapped.keys()).difference(facts_mapped)

            if bool(explainable_or):
                # do split with multiple OR-rules
                if explainable_or in MHS_CACHE.keys():
                    pass
                else:
                    splits_set = {
                        frozenset(or_rules_mapped[a]) for a in explainable_or}
                    splits_size = np.prod([len(s) for s in splits_set])
                    # only call MHS backend for large-size set
                    if splits_size > LIB_CALL:
                        MHS_CACHE[explainable_or] = solveSetCover_improved(
                            splits_set)
                    else:
                        MHS_CACHE[explainable_or] = solveSetCover(splits_set)
                v_set_new.update(doSplitMulti_cached(
                    exp, explainable_or, MHS_CACHE[explainable_or]))
                continue

            # just add to and_computable list
            v_list_andcomputable.append(exp)

        # update the list of explanations to be check again
        v_list_orcomputable = list(v_set_new)

        check_new_split = True
        while check_new_split:
            check_new_split = False
            v_set_new = set()
            for exp in v_list_orcomputable:
                # check final answer
                if exp.issubset(hypotheses_mapped):
                    final_set.add(exp.difference(facts_mapped))
                    continue

                # check split
                explainable_or = exp.intersection(or_rules_mapped.keys())
                if bool(explainable_or):
                    # do split with multiple OR-rules
                    if explainable_or in MHS_CACHE.keys():
                        pass
                    else:
                        splits_set = {
                            frozenset(or_rules_mapped[a]) for a in explainable_or}
                        splits_size = np.prod([len(s) for s in splits_set])
                        # only call MHS backend for large-size set
                        if splits_size > LIB_CALL:
                            MHS_CACHE[explainable_or] = solveSetCover_improved(
                                splits_set)
                        else:
                            MHS_CACHE[explainable_or] = solveSetCover(
                                splits_set)
                    v_set_new.update(doSplitMulti_cached(
                        exp, explainable_or, MHS_CACHE[explainable_or]))
                    check_new_split = True
                    continue

                # just add to another list
                v_list_andcomputable.append(exp)

            # update the list of explanations to be check again
            v_list_orcomputable = list(v_set_new)

        # join the two lists
        v_list_orcomputable = v_list_andcomputable + v_list_orcomputable
        # reproduce explanation matrix in sparse format
        len_v = len(v_list_orcomputable)
        nnz_idx = 0
        cur = 0
        for idx, exp in enumerate(v_list_orcomputable):
            len_exp = len(exp)
            pre_row[nnz_idx: nnz_idx + len_exp] = list(exp)
            pre_col[idx], pre_col[idx + 1] = cur, cur + len_exp
            pre_val[nnz_idx: nnz_idx + len_exp] = 1.0 / len_exp
            nnz_idx += len_exp
            cur += len_exp
        v = sparse.csc_matrix(
            (pre_val[: nnz_idx], pre_row[: nnz_idx], pre_col[: len_v + 1]), shape=(matrix_size, len_v))


def doAbduction_sparse_inspect(ms, v, hypotheses_mapped, rules, and_rules_mapped, or_rules_mapped, atoms_mapping, timeout):
    final_set = set()
    matrix_size = len(atoms_mapping)
    MHS_CACHE = dict()
    MHS_CACHE_HIT = 0
    global time_accum
    global maximum_explanations_size
    maximum_explanations_size = 0
    global maximum_explanations_nnz
    maximum_explanations_nnz = 0
    global minimum_explanations_sparsity
    minimum_explanations_sparsity = 1.0
    global max_iterations
    max_iterations = 0
    while True:
        v_new = sparse.csc_matrix.dot(ms.T, v)

        # check for equality and return
        if np.sum(np.absolute(v - v_new)) == 0:
            return eliminate(final_set), False
        # check shape is too large and return
        if v.shape[1] > EARLY_STOP:
            print("Early stopped due to", "Matrix size:", v.shape[1])
            return eliminate(final_set), True

        if v.nnz > maximum_explanations_nnz:
            maximum_explanations_nnz = v.nnz
        if 1.0 - v.nnz / (v.shape[0] * v.shape[1]) < minimum_explanations_sparsity:
            minimum_explanations_sparsity = 1.0 - \
                v.nnz / (v.shape[0] * v.shape[1])
        if v.shape[1] > maximum_explanations_size:
            maximum_explanations_size = v.shape[1]

        # asign v by v_new
        v = v_new
        print("Explanation matrix shape", v.shape, "non-zero elements", v.nnz, "total elements",
              v.shape[0] * v.shape[1], "sparsity", 1.0 - v.nnz / (v.shape[0] * v.shape[1]))

        v_list = list()
        test = v.sum(axis=0)
        for beg, end in zip(v.indptr[:-1], v.indptr[1:]):
            sum_check = np.sum(v.data[beg: end])
            # check unexplainable rule
            if sum_check < 0.999:
                continue
            exp = frozenset({int(item) for item in v.indices[beg: end]})
            v_list.append(exp)

        v_list_justcopy = list()
        check_new_split = True
        while check_new_split:
            check_new_split = False
            v_set_new = set()
            for exp in v_list:
                # check final answer
                if exp.issubset(hypotheses_mapped):
                    final_set.add(exp)
                    continue

                # check split
                explainable_or = exp.intersection(or_rules_mapped.keys())
                # check unexplainable rule
                explainable_and = exp.intersection(and_rules_mapped.keys())
                if not bool(explainable_and.union(explainable_or)):
                    # print("Eliminate unexplainable rule")
                    continue
                if bool(explainable_or):
                    # do split with multiple OR-rules
                    if explainable_or in MHS_CACHE.keys():
                        MHS_CACHE_HIT += 1
                    else:
                        splits_set = {
                            frozenset(or_rules_mapped[a]) for a in explainable_or}
                        splits_size = np.prod([len(s) for s in splits_set])
                        # only call MHS backend for large-size set
                        if splits_size > LIB_CALL:
                            MHS_CACHE[explainable_or] = solveSetCover_improved(
                                splits_set)
                        else:
                            MHS_CACHE[explainable_or] = solveSetCover(
                                splits_set)
                    v_set_new.update(doSplitMulti_cached(
                        exp, explainable_or, MHS_CACHE[explainable_or]))
                    check_new_split = True
                    continue

                # just add to another list
                v_list_justcopy.append(exp)

            # update the list of explanations to be check again
            v_list = list(v_set_new)

        # join the two lists
        v_list = v_list_justcopy + v_list
        # reproduce explanation matrix in sparse format
        start = time.time()
        len_v = len(v_list)
        nnz_idx = 0
        cur = 0
        for idx, exp in enumerate(v_list):
            len_exp = len(exp)
            pre_row[nnz_idx: nnz_idx + len_exp] = list(exp)
            pre_col[idx], pre_col[idx + 1] = cur, cur + len_exp
            pre_val[nnz_idx: nnz_idx + len_exp] = 1.0 / len_exp
            nnz_idx += len_exp
            cur += len_exp
        v = sparse.csc_matrix(
            (pre_val[: nnz_idx], pre_row[: nnz_idx], pre_col[: len_v + 1]), shape=(matrix_size, len_v))
        time_accum += time.time() - start
        print("Time accum", time_accum)
        max_iterations += 1
        print("MHS_CACHE_HIT", MHS_CACHE_HIT)


def initObservableVector(observations, atoms_mapping):
    v = np.zeros((len(atoms_mapping), 1), dtype=float)
    for obs in observations:
        v[atoms_mapping[obs]] = 1.0
    return v


def initObservableVector_sparse(observations, atoms_mapping):
    v = np.zeros((len(atoms_mapping), 1), dtype=float)
    row, col, val = [], [], []
    for obs in observations:
        row.append(atoms_mapping[obs])
        col.append(0)
        val.append(1.0)
    return sparse.coo_matrix((val, (row, col)), shape=(len(atoms_mapping), 1)).tocsc()


def createExplanation(exp, atoms_mapping):
    v = np.zeros((len(atoms_mapping), 1), dtype=float)
    for atom in exp:
        v[atom] = 1
    return v


def verifyExplanation(mp, v):
    while True:
        v_new = np.dot(mp, v)
        # check for equality and return
        if np.array_equal(v_new, v):
            return v
        # asign v by v_new
        v = v_new


def verifyExplanation_sparse(ms, v):
    while True:
        v_new = sparse.csr_matrix.dot(ms, v)
        # check for equality and return
        if np.array_equal(v_new > 0, v > 0):
            return v
        # asign v by v_new
        v = v_new


def doVerification(mp, v, observations, atoms_mapping):
    final_exp = verifyExplanation(mp, v)
    obs = observations.pop()
    observations.add(obs)
    ans = list()
    for i in range(final_exp.shape[1]):
        if final_exp[atoms_mapping[obs], i] > 0:
            ans.append(True)
        else:
            ans.append(False)
    return ans


def doVerification_sparse(ms, v, observations, atoms_mapping):
    final_exp = verifyExplanation_sparse(ms, v)
    obs = observations.pop()
    observations.add(obs)
    ans = list()
    for i in range(final_exp.shape[1]):
        if final_exp[atoms_mapping[obs], i] > 0:
            ans.append(True)
        else:
            ans.append(False)
    return ans


def writeOutput(method, ifile, ofile, hypotheses, effects, rules, and_rules, or_rules,
                observations, executation_time,
                explanations, out_atom_mapping, out_rules, nnz, sparsity, maximum_explanations_size,
                maximum_explanations_nnz, minimum_explanations_sparsity):
    import pandas as pd
    df = pd.DataFrame(columns=['result',	'PID',	'filename',	'hypotheses_size',	'effect_size',	'rules',	'obs_size',
                               'time',	'diagnosis_size',	'single_fault',	'double_fault',	'triple_fault',	'rest_fault',
                               'info1',	'info2',	'mem_consumption',
                               'standardized_atoms_size', 'standardized_rules_size',
                               'standardized_and_rules_size', 'standardized_or_rules_size',
                               'program_matrix_size', 'program_nnz', 'program_sparsity',
                               'maximum_explanations_size', 'maximum_explanations_nnz', 'minimum_explanations_sparsity',
                               'max_iterations'])
    ifile_extracted = os.path.split(ifile)
    # write file if not exist
    if not os.path.isfile(ofile):
        df.to_csv(ofile, index=False)
    for e in observations:
        break
    # write file
    df = df.append({'result': method, 'PID': None, 'filename': ifile_extracted[1],
                    'hypotheses_size': len(hypotheses), 'effect_size': len(effects), 'rules': len(rules), 'obs_size': len(and_rules[e]),
                    'time': executation_time,	'diagnosis_size': len(explanations),
                    'single_fault': None, 'double_fault': None, 'triple_fault': None, 'rest_fault': None,
                    'info1': None, 'info2': None,	'mem_consumption': None,
                    'standardized_atoms_size': len(out_atom_mapping), 'standardized_rules_size': len(out_rules),
                    'standardized_and_rules_size': len(and_rules), 'standardized_or_rules_size': len(or_rules),
                    'program_matrix_size': len(out_atom_mapping), 'program_nnz': nnz, 'program_sparsity': sparsity,
                    'maximum_explanations_size': maximum_explanations_size, 'maximum_explanations_nnz': maximum_explanations_nnz,
                    'minimum_explanations_sparsity': minimum_explanations_sparsity, 'max_iterations': max_iterations}, ignore_index=True)
    df.iloc[[-1]].to_csv(ofile, index=False, header=None, mode="a")
    pass


def abduce(ifile, ofile, timeout):
    rawdata, hypotheses, effects, observations, rules, facts = readInputData(
        ifile)
    start_time = time.time()
    sorted_rules = sorted(rules)
    atoms_mapping = doAtommapping(sorted_rules)
    out_rules, out_atom_mapping = standardizeRules(sorted_rules, atoms_mapping)
    hypotheses_mapped = mapAtomsToIndices(
        out_atom_mapping, hypotheses, frozenset)
    and_rules = {h[1][0]: h[2] for h in out_rules if h[0] == 'and'}
    and_rules_mapped = mapRulesToIndices(out_atom_mapping, and_rules, dict)
    or_rules = {h[1][0]: h[2] for h in out_rules if h[0] == 'or'}
    or_rules_mapped = mapRulesToIndices(out_atom_mapping, or_rules, dict)
    print("All rules size", len(out_rules), "\t Or-rules size", len(or_rules))
    if os.path.split(ifile)[1] in excluding_files:
        writeOutput("Dense matrix", ifile, ofile, hypotheses, effects, rules, and_rules, or_rules,
                    observations, "Timeout", list(), out_atom_mapping, out_rules, None, None, None, None, None)
        return rules, out_rules, out_atom_mapping, None, observations, set(), 0, True
    mp = buildMatrix(out_rules, out_atom_mapping)
    v = initObservableVector(observations, out_atom_mapping)
    start_time = time.time()
    explanations, maximum_explanations_size, is_timedout = doAbduction(
        mp, v, hypotheses_mapped, out_rules, and_rules_mapped, or_rules_mapped, out_atom_mapping, timeout)
    executation_time = time.time() - start_time

    writeOutput("Dense matrix", ifile, ofile, hypotheses, effects, rules, and_rules, or_rules, observations, executation_time,
                explanations, out_atom_mapping, out_rules, None, None, maximum_explanations_size, None, None)
    return rules, out_rules, out_atom_mapping, mp, observations, explanations, executation_time, is_timedout


def abduce_sparse(ifile, ofile, timeout):
    rawdata, hypotheses, effects, observations, rules, facts = readInputData(
        ifile)
    start_time = time.time()
    sorted_rules = sorted(rules)
    atoms_mapping = doAtommapping(sorted_rules)
    out_rules, out_atom_mapping = standardizeRules(sorted_rules, atoms_mapping)
    hypotheses_mapped = mapAtomsToIndices(
        out_atom_mapping, hypotheses, frozenset)
    facts_mapped = mapAtomsToIndices(out_atom_mapping, facts, frozenset)
    and_rules = {h[1][0]: h[2] for h in out_rules if h[0] == 'and'}
    and_rules_mapped = mapRulesToIndices(out_atom_mapping, and_rules, dict)
    or_rules = {h[1][0]: h[2] for h in out_rules if h[0] == 'or'}
    or_rules_mapped = mapRulesToIndices(out_atom_mapping, or_rules, dict)
    print("All rules size", len(out_rules), "\t Or-rules size", len(or_rules))
    ms = buildMatrix_sparse(out_rules, out_atom_mapping)
    vs = initObservableVector_sparse(observations, out_atom_mapping)
    fs = initObservableVector(facts, out_atom_mapping)

    if os.path.split(ifile)[1] in excluding_files:
        writeOutput("Sparse matrix", ifile, ofile, hypotheses, effects, rules, and_rules, or_rules,
                    observations, "Timeout", list(), out_atom_mapping, out_rules, ms.nnz, 1.0 -
                    ms.nnz/(len(out_atom_mapping) ** 2), None,
                    None, None)
        return rules, out_rules, out_atom_mapping, ms, observations, set(), 0, True
    start_time = time.time()
    explanations, is_timedout = doAbduction_sparse(
        ms, vs, hypotheses_mapped, facts_mapped, fs, out_rules, and_rules_mapped, or_rules_mapped, out_atom_mapping, timeout)
    executation_time = time.time() - start_time

    writeOutput("Sparse matrix", ifile, ofile, hypotheses, effects, rules, and_rules, or_rules, observations, executation_time,
                explanations, out_atom_mapping, out_rules, ms.nnz, 1.0 - ms.nnz /
                (len(out_atom_mapping) ** 2), maximum_explanations_size,
                maximum_explanations_nnz, minimum_explanations_sparsity)
    return rules, out_rules, out_atom_mapping, ms, observations, explanations, executation_time, is_timedout


def main():
    parser = argparse.ArgumentParser(
        description='Linear Algebraic Abduction Tool')
    parser.add_argument('input', help='Input file')
    parser.add_argument('output', help='Output file')
    parser.add_argument('format', choices=['dense', 'sparse', 'sparsedense'])
    parser.add_argument('--stop', type=int, default=20000,
                        help='Early stop based on explanations matrix size')

    args = parser.parse_args()

    loadpath = args.input
    writepath = args.output
    method = args.format
    timeout = 3600.0

    global EARLY_STOP
    EARLY_STOP = args.stop

    print(os.path.basename(loadpath), method.capitalize() + ":",
          "Run_start:", time.strftime("%Y/%m/%d %H:%M:%S", time.gmtime()))

    if method == "dense":
        rules, out_rules, out_atom_mapping, mp, observations, explanations, executation_time, is_timedout = abduce(
            loadpath, writepath, timeout)
        if is_timedout:
            print("Timed out.")
            sys.exit()
        print("Verified explanations:")
        v_exp = np.empty((0, len(out_atom_mapping))).T
        for exp in explanations:
            v = createExplanation(exp, out_atom_mapping)
            v_exp = np.append(v_exp, v, axis=1)
        print(doVerification(mp, v_exp, observations, out_atom_mapping))
    elif method == "sparse":
        rules, out_rules, out_atom_mapping, ms, observations, explanations, executation_time, is_timedout = abduce_sparse(
            loadpath, writepath, timeout)
        if is_timedout:
            print("Timed out.")
            sys.exit()
        print("Verified explanations:", end="")
        v_exp = np.empty((0, len(out_atom_mapping))).T
        for exp in explanations:
            v = createExplanation(exp, out_atom_mapping)
            v_exp = np.append(v_exp, v, axis=1)
        print(doVerification_sparse(ms, v_exp, observations, out_atom_mapping))
    print("Executation time", executation_time)
    # print("Explanations:")
    # for exp in explanations:
    #     sorted_exp = sorted(list(mapAtomsToSymbols(out_atom_mapping, exp, list)), key=lambda x: (int(x[2:]), x))
    #     print(sorted_exp)
    print("Delta: ", end="")
    delta_str = ','.join(map(str, [sorted(list(mapAtomsToSymbols(out_atom_mapping, exp, list)), key=lambda x: (
        int(x[2:]), x)) for exp in explanations])).replace("\'", "")
    print(delta_str)

    print(os.path.basename(loadpath), method.capitalize() + ":",
          "Run_ende:", time.strftime("%Y/%m/%d %H:%M:%S", time.gmtime()))


def main1():
    parser = argparse.ArgumentParser(
        description='Conduct experiment on all sample dataset in a folder')
    parser.add_argument('input', help='Input folder')
    parser.add_argument('output', help='Output file')
    parser.add_argument('format', choices=['dense', 'sparse'])

    args = parser.parse_args()

    loadpath = args.input
    writepath = args.output
    format = args.format

    import pandas as pd
    df = pd.DataFrame(columns=['filename', 'diagnosis_set', 'match',
                      'our_diagnosis_set', 'diff1', 'diff2', 'union', 'time'])
    df.to_csv(writepath)

    for idx, file in enumerate(os.listdir(loadpath)):
        path = os.path.join(loadpath, file)
        _, file_extension = os.path.splitext(path)
        if file_extension == ".atms":
            print("Working on: ", path)

            if format == 'dense':
                rules, out_rules, out_atom_mapping, mp, observations, explanations, executation_time, is_timedout = abduce(
                    path, writepath, 5)
            elif format == 'sparse':
                rules, out_rules, out_atom_mapping, ms, observations, explanations, executation_time, is_timedout = abduce_sparse(
                    path, writepath, 5)

            for exp in explanations:
                sorted_exp = sorted(list(mapAtomsToSymbols(
                    out_atom_mapping, exp, list)), key=lambda x: (int(x[2:]), x))
                print(sorted_exp)
            explanations_symbolics = {frozenset(
                {out_atom_mapping.inverse[atom] for atom in exp}) for exp in explanations}
            results_our = [sorted(list(re_exp), key=lambda x: (
                int(x[2:]), x)) for re_exp in explanations_symbolics]

            v = set()
            union_set = v.union(explanations_symbolics)
            union_list = [sorted(list(re_exp), key=lambda x: (
                int(x[2:]), x)) for re_exp in union_set]
            diff1_set = v.difference(explanations_symbolics)
            diff1_list = [sorted(list(re_exp), key=lambda x: (
                int(x[2:]), x)) for re_exp in diff1_set]
            diff2_set = explanations_symbolics.difference(v)
            diff2_list = [sorted(list(re_exp), key=lambda x: (
                int(x[2:]), x)) for re_exp in diff2_set]

            if is_timedout:
                timestr = "Timeout"
            else:
                timestr = str(executation_time * 1000)

            df = df.append({'filename': file, 'diagnosis_set': [], 'match': None, 'our_diagnosis_set': results_our,
                           'diff1': diff1_list, 'diff2': diff2_list, 'union': union_list, 'time': timestr}, ignore_index=True)
            df.iloc[[-1]].to_csv(writepath, header=None, mode="a")

            print("Processed", idx + 1, "/", len(os.listdir(loadpath)), "files")
            print("------------------------------------------")
            print("------------------------------------------")


if __name__ == "__main__":
    main1()
