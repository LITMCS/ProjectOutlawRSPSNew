package com.client;

import java.net.URL;
import java.net.URLConnection;
import java.util.zip.*;

import com.client.loader.Loader;
import com.client.sign.Signlink;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.io.*;
import javax.swing.JOptionPane;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

public class VersionControl {

    public static final String VERSION_FILE = Signlink.getCacheDirectory() + "cache_version.dat";
    private Client client;

    public VersionControl(Client client) {
        this.client = client;
    }
    
    public void main(String args[]) {
            try {
            		File file = new File(Signlink.getCacheDirectory() + "/media_archives");
                double newest = getNewestVersion();
                if (newest > getCurrentVersion()) {
	                updateClient();
	                drawLoadingText(100, "Fetching Updates - 100%");
	            }
                OutputStream out = new FileOutputStream(VERSION_FILE);
                out.write(String.valueOf(cacheVersion).getBytes());
                writeVersion(newest);
                out.close();
            } catch (Exception e) {
                handleException(e);
            }
    }

    private void drawLoadingText(int amount, String text) {
        client.drawLoadingText(amount, text);
    }

    public double getCurrentVersion() {
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(VERSION_FILE)));
            return Double.parseDouble(br.readLine());
        } catch (Exception e) {
            return 0.1;
        }
    }

    public static double getNewestVersion() throws ParserConfigurationException, IOException, SAXException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setValidating(true);
        factory.setIgnoringElementContentWhitespace(true);
        URL url = new URL("http://os-ps.org/config/config.xml");
        DocumentBuilder db = factory.newDocumentBuilder();
        Document doc = db.parse(url.openStream());
        NodeList nList = doc.getElementsByTagName("data");
        for (int temp = 0; temp < nList.getLength(); temp++) {
            Node nNode = nList.item(temp);
            if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                Element eElement = (Element) nNode;
                cacheVersion = Double.parseDouble(eElement
                        .getElementsByTagName("cache_version")
                        .item(0)
                        .getTextContent());
            }
        }
        if (cacheVersion > 0) {
            System.out.println("Cache version : " + cacheVersion);
        }
        return cacheVersion;
    }

    private static void fatal(String s) {
        System.err.println(s);
        System.exit(1);
    }

    private static void usage() {
        fatal("Usage: java JarRunner url [args..]");
    }

    private void handleException(Exception e) {
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

    private void alert(String title, String msg, boolean error) {
        JOptionPane.showMessageDialog(null,
                msg,
                title,
                (error ? JOptionPane.ERROR_MESSAGE : JOptionPane.PLAIN_MESSAGE));
    }

    public VersionControl writeVersion(double cacheVersion) {
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

    private void updateClient() {
        File clientZip = updateCache();
        if (clientZip != null) {
            unZip(clientZip);

        }
    }

    private void unZip(File clientZip) {
        try {
            unZipFile(clientZip, new File(Signlink.getCacheDirectory()));
            clientZip.delete();
        } catch (IOException e) {
            handleException(e);
        }
    }

    private void unZipFile(File zipFile, File outFile) throws IOException {
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

    public int percent = 0;

    public void setDownloadPercent(int amount) {
        percent = amount;
        drawLoadingText(amount, "Fetching Updates" + " - " + amount + "%");
    }

    public int percent2 = 0;

    public void setUnzipPercent(int amount2) {
        percent2 = amount2;
        drawLoadingText(amount2, "Fetching Updates" + " - " + amount2 + "%");
    }

    private File updateCache() {
        File ret = new File(Signlink.getCacheDirectory() + "Ascend.zip");
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
                setDownloadPercent((int) ((curr * 100) / max));
            }
            in.close();
            double newest = getNewestVersion();
            out = new FileOutputStream(VERSION_FILE);
            out.write(String.valueOf(cacheVersion).getBytes());
            writeVersion(newest);
            out.flush();
            out.close();
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