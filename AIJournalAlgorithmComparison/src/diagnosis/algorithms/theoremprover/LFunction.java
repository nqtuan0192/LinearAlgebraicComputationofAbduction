/*
 * (c) copyright 2008, Technische Universitaet Graz and Technische Universitaet Wien
 *
 * This file is part of jdiagengine.
 *
 * jdiagengine is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * jdiagengine is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with jdiagengine. If not, see <http://www.gnu.org/licenses/>.
 *
 * Authors: Joerg Weber, Franz Wotawa
 * Contact: jweber@ist.tugraz.at (preferred), or fwotawa@ist.tugraz.at
 *
 */


/**
 * LFunction: Implements an object representing a function
 *
 * @version 0.1, DATE: 230.12.1998
 * @author Franz Wotawa
 *
 * 
 * V0.1: Creating the basic functionality (30.12.1998)
 */

package diagnosis.algorithms.theoremprover;

import java.lang.*;    // Java language classes 
import java.util.*;



public class LFunction extends LConstant
{
    // Instance variables

    public ArrayList arguments;

    // Instance creation and initialization

    LFunction()
    {
	identifier = null;
	arguments = new ArrayList();
    }

    LFunction(String str, ArrayList v)
    {
	identifier = str;
	arguments = v;
    }

    // Accessing methods

    public String toString()
    {
	StringBuffer str = new StringBuffer();
	str.append(identifier);
	if (arguments.size() != 0) {
	  str.append("(");
	  Iterator e = arguments.iterator();
	  int i = arguments.size();
	  while (e.hasNext()) {
	    str.append((e.next()).toString());
	    i--;
	    if (i>0) { str.append(", ");}
	  }
	  str.append(")");
	}
	return str.toString();
    }

    public PropositionalTheoremProver asPropositionalSentence()
    {
	return null;
    }
}
