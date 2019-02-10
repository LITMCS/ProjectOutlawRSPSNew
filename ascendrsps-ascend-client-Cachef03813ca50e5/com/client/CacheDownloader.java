package com.client;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.FileWriter;
import java.io.BufferedWriter;
import java.io.BufferedOutputStream;
import java.io.BufferedInputStream;
import java.io.FileOutputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import com.client.sign.Signlink;

public class CacheDownloader {

	private Client client;

	private final int BUFFER = 1024;

	private final int VERSION = 3;
	private String CACHE_LINK = Configuration.CACHE_LINK;

	private Path FILE_LOCATION = Paths.get(getCacheDir(), getArchivedName());

	public CacheDownloader(Client client) {
		this.client = client;
	}

	private void drawLoadingText(String text) {
		client.drawLoadingText(35, text);
		System.out.println(text);
	}

	private void drawLoadingText(int amount, String text, int downloadSpeed, int timeRemaining) {
		client.drawLoadingText(amount, text);
	}

	private String getCacheDir() {
		return Signlink.getCacheDirectory();
	}

	private String getCacheLink() {
		return CACHE_LINK;
	}

	private int getCacheVersion() {
		return VERSION;
	}

	public CacheDownloader downloadCache() {
		try {
			File location = new File(getCacheDir());
			File version = new File(getCacheDir() + "/cacheVersion" + getCacheVersion() + ".dat");
			if (!location.exists()) {
				System.out.println("Location does not exist, downloading.");
				downloadFile(getCacheLink(), getArchivedName());

				unZip();

				BufferedWriter versionFile = new BufferedWriter(new FileWriter(
						getCacheDir() + "/cacheVersion" + getCacheVersion()
								+ ".dat"));
				versionFile.close();
			} else {
				if (!version.exists()) {
					downloadFile(getCacheLink(), getArchivedName());

					unZip();

					BufferedWriter versionFile = new BufferedWriter(
							new FileWriter(getCacheDir() + "/cacheVersion"
									+ getCacheVersion() + ".dat"));
					versionFile.close();
				} else {
					return null;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	private void downloadFile(String adress, String localFileName) {
		OutputStream out = null;
		URLConnection conn;
		InputStream in = null;
		
		try {
			URL url = new URL(adress);
			out = new BufferedOutputStream(new FileOutputStream(getCacheDir() + "/" + localFileName));

			conn = url.openConnection();
			in = conn.getInputStream();

			byte[] data = new byte[BUFFER];

			int numRead;
			long numWritten = 0;
			int fileSize = conn.getContentLength();
			long startTime = System.currentTimeMillis();
	
			while ((numRead = in.read(data)) != -1) {
				out.write(data, 0, numRead);
				numWritten += numRead;

				int percentage = (int) (((double) numWritten / (double) fileSize) * 100D);
				long elapsedTime = System.currentTimeMillis() - startTime;
				int downloadSpeed = (int) ((numWritten / 1024) / (1 + (elapsedTime / 1000)));
				
				float speedInBytes = 1000f * numWritten / elapsedTime;
				int timeRemaining =  (int) ((fileSize - numWritten) / speedInBytes);
				
				drawLoadingText(percentage, "Ascend - Downloading Cache " + percentage + "%", downloadSpeed, timeRemaining);
			}
			System.out.println(localFileName + "\t" + numWritten);
			drawLoadingText("Ascend - Unzipping...");
		} catch (Exception exception) {
			exception.printStackTrace();
		} finally {
			try {
				if (in != null) {
					in.close();
				}
				if (out != null) {
					out.close();
				}
			} catch (IOException ioe) {
				ioe.printStackTrace();
			}
		}
	}

	private String getArchivedName() {
		int lastSlashIndex = getCacheLink().lastIndexOf('/');
		if (lastSlashIndex >= 0 && lastSlashIndex < getCacheLink().length() - 1) {
			String u = getCacheLink().substring(lastSlashIndex + 1);
			String Name = u.replace("?dl=1", "");
			return Name;
		} else {
			System.err.println("error retrieving archived name.");
		}
		return "";
	}

	private void unZip() throws IOException {
		InputStream in = new BufferedInputStream(new FileInputStream(FILE_LOCATION.toString()));
		ZipInputStream zin = new ZipInputStream(in);
		ZipEntry e;
		while ((e = zin.getNextEntry()) != null) {
			String fileName = e.getName();
			File newFile = new File(getCacheDir() + File.separator + fileName);
			if (e.isDirectory()) {
				(new File(getCacheDir() + e.getName())).mkdir();
			} else {

				if (e.getName().equals(FILE_LOCATION)) {
					unzip(zin, FILE_LOCATION.toString());
					break;
				}
				new File(newFile.getParent()).mkdirs();
				unzip(zin, getCacheDir() + e.getName());
			}
			System.out.println("unzipping2 " + e.getName());
		}
		zin.close();
		Files.deleteIfExists(FILE_LOCATION);
	}

	private void unzip(ZipInputStream zin, String s) throws IOException {
		FileOutputStream out = new FileOutputStream(s);
		byte[] b = new byte[BUFFER];
		int len = 0;
		while ((len = zin.read(b)) != -1)
			out.write(b, 0, len);
		
		out.close();
	}
	
}