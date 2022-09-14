package diagnosis.algorithms;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

public class ClingoInterface {

	public StringBuilder output;
	public Process process;

	public StringBuilder executeClingo(List<String> commands) throws IOException {
		InputStream is;
		String line;
		this.output = new StringBuilder();
		ProcessBuilder processbuilder = new ProcessBuilder(commands);
		this.process = processbuilder.start();
		try {
			this.process.waitFor();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		is = this.process.getErrorStream();
		InputStreamReader isr2 = new InputStreamReader(is);
		BufferedReader br2 = new BufferedReader(isr2);
		while (((line = br2.readLine()) != null)) {
			this.output.append(line + "\n");
		}

		is = this.process.getInputStream();
		InputStreamReader isr = new InputStreamReader(is);
		BufferedReader br = new BufferedReader(isr);
		while (((line = br.readLine()) != null)) {
			this.output.append(line + "\n");
		}

		this.process.destroy();
		return this.output;

	}
}
