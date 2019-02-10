package com.client.loader;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

/**
 * @author Cody Reichenbach
 * 
 */
public class Download {

	public void start() throws IOException {
		visitSite();
	}

	private void visitSite() throws IOException {
		String url = "http://os-ps.org/downloads/gamepacks/osps_gamepack_9.jar";
		URL u = new URL(url);
		BufferedReader in = new BufferedReader(new InputStreamReader(u.openStream()));

		String read;
		while ((read = in.readLine()) != null) {
			if (!read.contains("archive=\""))
				continue;
			String arch = read.split("archive=\"")[1];
			arch = arch.split("\"")[0];
			URL uu = new URL("http://os-ps.org" + arch);
			int length = getFileSize(uu);
			URLConnection conn = uu.openConnection();
			conn.setDoOutput(true);
			conn.setDoInput(true);
			conn.setRequestProperty("content-type", "binary/data");
			InputStream inn = conn.getInputStream();
			FileOutputStream out = new FileOutputStream("./osps.jar");

			byte[] b = new byte[1024];
			int count;
			int down = 0;
			while ((count = inn.read(b)) > 0) {
				out.write(b, 0, count);
				down += count;
				Loader.drawLoadingText(percentage(down, length), "Downloading OSPS - " + percentage(down, length) + "%");
			}
			out.close();
			inn.close();
		}
		in.close();
	}

	private int percentage(int current, int length) {
		return (current * 100) / length;
	}

	private int getFileSize(URL url) {
		HttpURLConnection conn = null;
		try {
			conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("HEAD");
			conn.getInputStream();
			return conn.getContentLength();
		} catch (IOException e) {
			return -1;
		} finally {
			conn.disconnect();
		}
	}

}
