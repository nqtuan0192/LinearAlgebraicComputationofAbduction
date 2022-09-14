package diagnosis.algorithms;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

/*import org.nabelab.solar.CFP;
import org.nabelab.solar.Clause;
import org.nabelab.solar.Conseq;
import org.nabelab.solar.Env;
import org.nabelab.solar.ExitStatus;
import org.nabelab.solar.Options;
import org.nabelab.solar.SOLAR;
import org.nabelab.solar.parser.ParseException;*/

import diagnosis.engines.ConsquenceFindingEngine.SearchStrategy;

public class SOLARInterface {

	public StringBuilder output;
	public Process process;

	public StringBuilder executeSolar(String fileArg, String execDir, SearchStrategy strategy, long timeout)
			throws IOException {
		InputStream is;
		String line;
		this.output = new StringBuilder();
		String command = execDir + "solar2-build315.jar";
		timeout = timeout / 1000;
		ProcessBuilder pb;
		if (strategy.equals(SearchStrategy.DFIDR)) {
			if (timeout > 0) {
				pb = new ProcessBuilder("java", "-jar", command, "-v", "-t", String.valueOf(timeout), "-dfidr",
						String.valueOf(100000), fileArg);
			} else {
				pb = new ProcessBuilder("java", "-jar", command, "-v", "-num", String.valueOf(100000), "-dfidr",
						String.valueOf(100000), fileArg);
			}
		} else if (strategy.equals(SearchStrategy.DF)) {
			if (timeout > 0) {
				pb = new ProcessBuilder("java", "-jar", command, "-v", "-t", String.valueOf(timeout), "-df", fileArg);
			} else {
				pb = new ProcessBuilder("java", "-jar", command, "-v", "-df", fileArg);
			}
		} else {
			if (timeout > 0) {
				pb = new ProcessBuilder("java", "-jar", command, "-v", "-t", String.valueOf(timeout), fileArg);
			} else {
				pb = new ProcessBuilder("java", "-jar", command, "-v", fileArg);
			}
		}

		this.process = pb.start();
		is = this.process.getInputStream();
		InputStreamReader isr = new InputStreamReader(is);
		BufferedReader br = new BufferedReader(isr);
		while (((line = br.readLine()) != null)) {
			this.output.append(line + "\n");
		}

		InputStream err = this.process.getErrorStream();
		InputStreamReader iserr = new InputStreamReader(err);
		BufferedReader berr = new BufferedReader(iserr);
		while (((line = berr.readLine()) != null)) {
			this.output.append(line + "\n");
			
		}

		this.process.destroy();
		
		
		return this.output;

	}

	/*public StringBuilder executeSolarDirectly(String fileArg, String execDir, SearchStrategy strategy, long timeout)
			throws IOException {
		Env env = new Env();
		Options opt = new Options(env);
		this.output = new StringBuilder();
		timeout = timeout / 1000;
		
		//set options
		if (strategy.equals(SearchStrategy.DFIDR)) {
			if (timeout > 0) {
				String args[] = { "-t", String.valueOf(timeout), "-dfidr", String.valueOf(100000), fileArg };
				opt.parse(args);
			} else {
				String args[] = { "-num", String.valueOf(100000), "-dfidr", String.valueOf(100000), fileArg };
				opt.parse(args);
			}
		} else if (strategy.equals(SearchStrategy.DF)) {
			if (timeout > 0) {
				String args[] = { "-t", String.valueOf(timeout), "-df", fileArg };
				opt.parse(args);
			} else {
				String args[] = { "-df", fileArg };
				opt.parse(args);
			}
		} else {
			if (timeout > 0) {
				String args[] = { "-t", String.valueOf(timeout), fileArg };
				opt.parse(args);
			} else {
				String args[] = { fileArg };
				opt.parse(args);
			}
		}

		//create consequence finding problem from file
		CFP cfp = new CFP(env, opt);
		try {
			cfp.parse(new File(fileArg));
		} catch (ParseException e) {
			e.printStackTrace();
		}

		SOLAR solar = new SOLAR(env, cfp);

		//change system out stream for a short period of time to suppress unwanted
		//debug output
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		PrintStream ps = new PrintStream(baos);
		PrintStream old = System.out;
		System.setOut(ps);
		
		try {
			//execute solar
			solar.exec();
		} catch (ParseException e) {
			e.printStackTrace();
		}
		//change system out stream back
		System.out.flush();
		System.setOut(old);

		//get status of the consequence finding problem and collect the wanted output
		int status = cfp.getStatus();
		List<Conseq> conqs = cfp.getConseqSet().get();

		switch (status) {
		case ExitStatus.UNSATISFIABLE:
			this.output.append("UNSATISFIABLE" + "\n");
			if (opt.hasProofOp()) {
				this.output.append("\n");
				this.output.append("PROOF:" + "\n");
				this.output.append(conqs.get(0).getProof() + "\n");
			}
			if (opt.hasUsedClausesOp()) {
				if (!opt.hasProofOp()) {
					this.output.append("\n");
				}
				this.output.append("USED CLAUSES:" + "\n");
				for (Clause used : conqs.get(0).getUsedClauses()) {
					this.output.append(used + "\n");
				}
			}

			break;
		case ExitStatus.SATISFIABLE:
			this.output.append("SATISFIABLE" + "\n");
			this.output.append("\n");
			if (conqs.isEmpty()) {
				this.output.append("NO CONSEQUENCES" + "\n");
			} else {
				if (cfp.getProblemType() == CFP.CHARACTERISTIC) {
					this.output.append(conqs.size() + " CHARACTERISTIC CLAUSES" + "\n");
				} else {
					this.output.append(conqs.size() + " FOUND CONSEQUENCES" + "\n");
				}
				if (opt.getOutputFile() == null) {
					for (Conseq c : conqs) {
						this.output.append(c + "\n");
						if (opt.hasProofOp()) {
							this.output.append("\n");
							this.output.append("PROOF:" + "\n");
							this.output.append(c.getProof() + "\n");
						}
						if (opt.hasUsedClausesOp()) {
							if (!opt.hasProofOp()) {
								this.output.append("\n");
							}
							this.output.append("USED CLAUSES:" + "\n");
							for (Clause used : c.getUsedClauses()) {
								this.output.append(used + "\n");
							}
							this.output.append("\n");
						}
					}
				}
			}
			break;
		case ExitStatus.TRIVIALLY_SATISFIABLE:
			this.output.append("TRIVIALLY SATISFIABLE" + "\n");
			break;
		case ExitStatus.UNKNOWN:
			if (conqs.isEmpty()) {
				this.output.append("NOT FOUND" + "\n");
			} else {
				if (cfp.getProblemType() == CFP.CHARACTERISTIC) {
					this.output.append(conqs.size() + " CHARACTERISTIC CLAUSES" + "\n");
				} else {
					this.output.append(conqs.size() + " FOUND CONSEQUENCES" + "\n");
				}
				if (opt.getOutputFile() == null) {
					for (Conseq c : conqs) {
						this.output.append(c + "\n");
						if (opt.hasProofOp()) {
							this.output.append("\n");
							this.output.append("PROOF:" + "\n");
							this.output.append(c.getProof() + "\n");
						}
						if (opt.hasUsedClausesOp()) {
							if (!opt.hasProofOp()) {
								this.output.append("\n");
							}
							this.output.append("USED CLAUSES:" + "\n");
							for (Clause used : c.getUsedClauses()) {
								this.output.append(used + "\n");
							}
							this.output.append("\n");
						}
					}
				}
			}
			break;
		}
		this.output.append("\n");
		
		//get remaining wanted stats
		baos = new ByteArrayOutputStream();
		ps = new PrintStream(baos);
		solar.printStats(ps);
		String content = new String(baos.toByteArray(), StandardCharsets.UTF_8);
		this.output.append(content);
		System.out.println("DIRECT " + this.output);
		return this.output;

	}*/
}
