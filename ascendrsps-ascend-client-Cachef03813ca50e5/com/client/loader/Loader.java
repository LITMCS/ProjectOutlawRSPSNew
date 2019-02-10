package com.client.loader;

import java.applet.Applet;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.client.Client;
import com.client.Configuration;
import com.client.sign.Signlink;

public class Loader {

	 public static double getCurrentVersion() {
	        try {
	            BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(VERSION_FILE)));
	            return Double.parseDouble(br.readLine());
	        } catch (Exception e) {
	            return 0.1;
	        }
	    }

	public static double getNewestVersion() throws ParserConfigurationException, IOException, SAXException {
		try {
			URL url = new URL(Configuration.VERSION_URL);
			HttpURLConnection connection = (HttpURLConnection) url.openConnection();
			connection.addRequestProperty("User-Agent", "Mozilla/4.76");
			BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
			cacheVersion = Double.parseDouble(br.readLine());
			br.close();
		} catch (Exception localException) {
			localException.printStackTrace();
			System.out.println("We're getting an error!");
		}

		return cacheVersion;
	}

	    private static void fatal(String s) {
	        System.err.println(s);
	        System.exit(1);
	    }
	 
	 public static final String VERSION_FILE = Signlink.getCacheDirectory() + "cache_version.dat";
	 private static Client client;


	private static JFrame frame;
	
	public static void fetchCacheSub(int index) {
		switch (index) {
		case 0:
			downloadCache();
			break;
		case 1:
			downloadMainFileCache();
			break;
		case 2:
			downloadSpriteCache();
			break;
		case 3:
			downloadMediaArchives();
			break;
		}
	}
	
	public static void main(String[] args) {
        System.setProperty("http.agent", "Chrome");
		try {
			frame = new JFrame();
			if (System.getProperty("os.name").contains("ac")) {
				frame.setSize(320, 100);
			} else if (System.getProperty("os.name").contains("indow")) {
				frame.setSize(337, 113);
			}
			frame.setTitle(Configuration.CLIENT_TITLE);
			frame.setLocationRelativeTo(null);
			JPanel down = new JPanel();
			down.setLayout(null);
			down.setBackground(Color.black);
			frame.add(down, BorderLayout.CENTER);
			frame.setVisible(true);
			drawLoadingText(0, "Checking for updates - 0%");
			try {
	            double newest = getNewestVersion();
	            if (newest > getCurrentVersion()) {
	            		for (int i = 0; i < 4; i++) {
	            			fetchCacheSub(i);
	            		}
	                drawLoadingText(100, "Fetching updates - 100%");
	                frame.dispose();
	                Client.main(new String[] {""});
	            } else {
	            	    frame.dispose();
	                Client.main(new String[] {""});
	            }
	            OutputStream out = new FileOutputStream(VERSION_FILE);
	            out.write(String.valueOf(cacheVersion).getBytes());
	            writeVersion(newest);
	            out.close();
	        } catch (Exception e) {
	            handleException(e);
	        }
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void drawLoadingText(int i, String s)
	{
		Graphics graphics = frame.getContentPane().getGraphics();
		
		while(graphics == null)
		{
			graphics = frame.getContentPane().getGraphics();
			try
			{
				frame.getContentPane().repaint();
			}
			catch(Exception _ex) { }
			try
			{
				Thread.sleep(1000L);
			}
			catch(Exception _ex) { }
		}
		int minusX = 223;
		int minusY = 253;
		Font font = new Font("Helvetica", 1, 13);
		FontMetrics fontmetrics = frame.getContentPane().getFontMetrics(font);
		frame.getContentPane().getFontMetrics(new Font("Helvetica", 0, 13));
		
		graphics.setColor(Color.black);
		graphics.fillRect(0, 0, 585, 767);
		
		int j = 585 / 2 - 18;
		graphics.setColor(Color.decode("0x980000"));
		graphics.drawRect(767 / 2 - 152 - minusX, j - minusY, 304, 34);
		graphics.fillRect(767 / 2 - 150 - minusX, j + 2 - minusY, i * 3, 30);
		graphics.setColor(Color.black);
		graphics.fillRect((767 / 2 - 150) + i * 3 - minusX, j + 2 - minusY, 300 - i * 3, 30);
		graphics.setFont(font);
		graphics.setColor(Color.white);
		int byte1 = 20;
		int centerX = 450 / 2, centerY = 200 / 2;
		graphics.drawString(s, (444 - fontmetrics.stringWidth(s)) / 2 - 59, (centerY + 4 - 41) - byte1);
	}
	
	private static void usage() {
        fatal("Usage: java JarRunner url [args..]");
    }

    private static void handleException(Exception e) {
        StringBuilder strBuff = new StringBuilder();
        strBuff.append("Please Screenshot this message, and send it to an admin!\r\n\r\n");
        StringBuilder append = strBuff.append(e.getClass().getName()).append(" \"").append(e.getMessage()).append("\"\r\n");
        for (StackTraceElement s : e.getStackTrace())
            strBuff.append(s.toString()).append("\r\n");
        alert("Exception [" + e.getClass().getSimpleName() + "]", strBuff.toString(), true);
    }

    private void alert(String msg) {
        alert("Message", msg, false);
    }

    private static void alert(String title, String msg, boolean error) {
        JOptionPane.showMessageDialog(null,
                msg,
                title,
                (error ? JOptionPane.ERROR_MESSAGE : JOptionPane.PLAIN_MESSAGE));
    }

    public static Loader writeVersion(double cacheVersion) {
        try {
            File location = new File(Signlink.getCacheDirectory());
            if (!location.exists()) {
                OutputStream out = new FileOutputStream(VERSION_FILE);
                out.write(String.valueOf(cacheVersion).getBytes());
                out.close();
            } else {
                OutputStream out = new FileOutputStream(VERSION_FILE);
                out.write(String.valueOf(cacheVersion).getBytes());
                out.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private static void downloadCache() {
        File clientZip = updateCache();
        if (clientZip != null) {
            unZip(clientZip);
        }
    }
    
    private static void downloadMediaArchives() {
        File clientZip = fetchMediaArchives();
        if (clientZip != null) {
            unZip(clientZip);
        }
    }
    
    private static void downloadSpriteCache() {
        File clientZip = fetchSpriteCache();
        if (clientZip != null) {
            unZip(clientZip);
        }
    }
    
    private static void downloadMainFileCache() {
        File clientZip = fetchMainFileCache();
        if (clientZip != null) {
            unZip(clientZip);
        }
    }

    private static void unZip(File clientZip) {
        try {
            unZipFile(clientZip, new File(Signlink.getCacheDirectory()));
            clientZip.delete();
        } catch (IOException e) {
            handleException(e);
        }
    }

    private static void unZipFile(File zipFile, File outFile) throws IOException {
        ZipInputStream zin = new ZipInputStream(new BufferedInputStream(new FileInputStream(zipFile)));
        ZipEntry e;
        long max = 0;
        long curr = 0;
        while ((e = zin.getNextEntry()) != null)
            max += e.getSize();
        zin.close();
        ZipInputStream in = new ZipInputStream(new BufferedInputStream(new FileInputStream(zipFile)));
        while ((e = in.getNextEntry()) != null) {
            if (e.isDirectory())
                new File(outFile, e.getName()).mkdirs();
            else {
                FileOutputStream out = new FileOutputStream(new File(outFile, e.getName()));
                byte[] b = new byte[1024];
                int len;
                while ((len = in.read(b, 0, b.length)) > -1) {
                    curr += len;
                    out.write(b, 0, len);
                    setUnzipPercent((int) ((curr * 100) / max));
                }

                out.flush();
                out.close();
            }
        }
    }

    public static int percent = 0;

    public static void setDownloadPercent(int amount, String input, int amount2) {
        percent = amount;
        drawLoadingText(amount2, input + " - " + amount + "%");
    }

    public static int percent2 = 0;

    public static void setUnzipPercent(int amount2) {
        percent2 = amount2;
        drawLoadingText(0, "Fetching updates");
    }

    private static File fetchMediaArchives() {
    	File ret = new File(Signlink.getCacheDirectory() + "media_archives.zip");
        try {
        		OutputStream out = new FileOutputStream(ret);
            URLConnection conn = new URL(Configuration.MEDIA_ARCHIVES_LINK).openConnection();
            InputStream in = conn.getInputStream();
            long max = conn.getContentLength();
            long curr = 0;
            byte[] b = new byte[1024];
            int len;
            while ((len = in.read(b, 0, b.length)) > -1) {
                out.write(b, 0, len);
                curr += len;
                setDownloadPercent((int) ((curr * 100) / max), "Fetching media archives", 82);
            }
            out.flush();
            out.close();
            in.close();
            return ret;
        } catch (Exception e) {
            handleException(e);
            ret.delete();
            return null;
        }
    }
    
    private static File fetchSpriteCache() {
    	File ret = new File(Signlink.getCacheDirectory() + "sprites.zip");
        try {
        		OutputStream out = new FileOutputStream(ret);
            URLConnection conn = new URL(Configuration.SPRITE_CACHE_URL).openConnection();
            InputStream in = conn.getInputStream();
            long max = conn.getContentLength();
            long curr = 0;
            byte[] b = new byte[1024];
            int len;
            while ((len = in.read(b, 0, b.length)) > -1) {
                out.write(b, 0, len);
                curr += len;
                setDownloadPercent((int) ((curr * 100) / max), "Fetching sprite cache", 61);
            }
            out.flush();
            out.close();
            in.close();
            return ret;
        } catch (Exception e) {
            handleException(e);
            ret.delete();
            return null;
        }
    }
    
    private static File fetchMainFileCache() {
    	File ret = new File(Signlink.getCacheDirectory() + "main_file_cache.zip");
        try {
        		OutputStream out = new FileOutputStream(ret);
            URLConnection conn = new URL(Configuration.MAIN_FILE_CACHE_URL).openConnection();
            InputStream in = conn.getInputStream();
            long max = conn.getContentLength();
            long curr = 0;
            byte[] b = new byte[1024];
            int len;
            while ((len = in.read(b, 0, b.length)) > -1) {
                out.write(b, 0, len);
                curr += len;
                setDownloadPercent((int) ((curr * 100) / max), "Fetching main file cache", 34);
            }
            out.flush();
            out.close();
            in.close();
            return ret;
        } catch (Exception e) {
            handleException(e);
            ret.delete();
            return null;
        }
    }
    
    private static File updateCache() {
        File ret = new File(Signlink.getCacheDirectory() + "Ascend_.32.zip");
        try {
        		OutputStream out = new FileOutputStream(ret);
            URLConnection conn = new URL(Configuration.CACHE_LINK).openConnection();
            InputStream in = conn.getInputStream();
            long max = conn.getContentLength();
            long curr = 0;
            byte[] b = new byte[1024];
            int len;
            while ((len = in.read(b, 0, b.length)) > -1) {
                out.write(b, 0, len);
                curr += len;
                setDownloadPercent((int) ((curr * 100) / max), "Fetching updates", 10);
            }
            out.flush();
            out.close();
            in.close();
            return ret;
        } catch (Exception e) {
            handleException(e);
            ret.delete();
            return null;
        }
    }

    public static String clientURL;
    public static double cacheVersion;
}