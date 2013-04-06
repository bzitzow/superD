/*******************************************************************************
 *  Copyright 2013 Jason Sipula and Trace Hagan                                *
 *                                                                             *
 *  Licensed under the Apache License, Version 2.0 (the "License");            *
 *  you may not use this file except in compliance with the License.           *
 *  You may obtain a copy of the License at                                    *
 *                                                                             *
 *      http://www.apache.org/licenses/LICENSE-2.0                             *
 *                                                                             *
 *  Unless required by applicable law or agreed to in writing, software        *
 *  distributed under the License is distributed on an "AS IS" BASIS,          *
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.   *
 *  See the License for the specific language governing permissions and        *
 *  limitations under the License.                                             *
 *******************************************************************************/

package net.snakedoc.superd;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;

import net.snakedoc.jutils.timer.MilliTimer;
import net.snakedoc.jutils.database.H2;
import net.snakedoc.jutils.system.SysInfo;;

public class DedupeR {

	/* THIS IS DEBUG MAIN()
	 * This should not be left in for deployment, only for development
	 * Logic in main() should be moved to it's own driver method so it can be invoked
	 * via scripts.
	 */
	public static void main (String[] args) {
		
		// get instance of MilliTimer()
		MilliTimer timer = new MilliTimer();
		
		// start timer
		timer.startTimer();
		
		// get instance of other helper objects
		H2 db = new H2();
		
		SysInfo sys = new SysInfo();
		DeDupeSQL sql = new DeDupeSQL();
		CheckDupes check = new CheckDupes();
		
		// this needs to be fixed so that the user passes
		// in the argument for hashVer...
		//
		// also, a better name for hashVer is probably hashAlgo
		// since it's not a version, its an algorithm we are selecting
		//
		// options being MD2, MD5, SHA1, SHA-256, SHA-384, SHA-512
		// there is a method in Hasher (from jutils) that will validate
		// the user input to ensure it's a supported hash algorithm.
		String hashVer = "SHA-";
		if (sys.getCPUArch().contains("64")) {
			hashVer += "512";
		} else {
			hashVer += "256";
		}
		
		try {
			db.openConnection();
		} catch (ClassNotFoundException | SQLException e) {
			// TODO change to log out (fatal)
			e.printStackTrace();
		}
		setup();
		check.checkDupes();
		try {
			db.closeConnection();
		} catch (SQLException e) {
			// TODO change to log out (warning)
			e.printStackTrace();
		}
		
		// stop timer
		timer.stopTimer();
		// TODO change to log out (info)
		System.out.println("Total Runtime: " + timer.getTime());
	}
	/* END DEBUG MAIN() */
	
	
	public static void setup() {
		
		/* TODO should not be limited to 1 directory to scan
		 * should allow deduping multiple root directories.
		 * this would be useful for deduping data sets that are
		 * on two or more drives.
		 */
		File[] rootDirs = new File[1];
		
		// TODO change to user specified root directory
		rootDirs[0] = new File("/home/jason");

		walk(rootDirs);
	}
	public static void walk(File[] files) {
		for (int i = 0; i < files.length; i++) {
			try {
				if (files[i].getAbsolutePath().equalsIgnoreCase(files[i].getCanonicalPath())) {
					if (!(files[i].toString().contains("/."))) {
						walk(DirectoryScanner.getList(files[i].toString(), st), deDupeObj, st);
					}
				}
			} catch (NullPointerException | IOException e) {
				continue;
			}
		}
	}
}
