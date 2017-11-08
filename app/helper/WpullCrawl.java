/*
 * Copyright 2014 hbz NRW (http://www.hbz-nrw.de/)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package helper;

import models.Gatherconf;
import models.Globals;
import models.Node;
import play.Logger;
import play.Play;

import java.io.*;
import java.lang.ProcessBuilder;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * a class to implement a wpull crawl
 * 
 * @author Ingolf Kuss
 *
 */
public class WpullCrawl {

	@SuppressWarnings("javadoc")
	public enum CrawlControllerState {
		NEW, RUNNING, PAUSED, ABORTED, CRASHED, FINISHED
	}

	private Gatherconf conf = null;
	private String date = null;
	private String datetime = null;
	private File crawlDir = null;
	private String localpath = null;
	private String warcFilename = null;
	private int exitState = 0;
	private String msg = null;

	final static String jobDir =
			Play.application().configuration().getString("regal-api.wpull.jobDir");
	final static String crawler =
			Play.application().configuration().getString("regal-api.wpull.crawler");

	private static final Logger.ALogger WebgatherLogger =
			Logger.of("webgatherer");

	public File getCrawlDir() {
		return crawlDir;
	}

	public String getLocalpath() {
		return localpath;
	}

	public int getExitState() {
		return exitState;
	}

	/**
	 * a constructor of a wpull crawl
	 * 
	 * @param conf the crawler configuration for the website
	 */
	public WpullCrawl(Gatherconf conf) {
		this.conf = conf;
	}

	/**
	 * creates a new wpull crawler job
	 */
	public void createJob() {
		WebgatherLogger.debug("Create new job " + conf.getName());
		try {
			if (conf.getName() == null) {
				throw new RuntimeException("The configuration has no name !");
			}
			date = new SimpleDateFormat("yyyyMMdd").format(new java.util.Date());
			datetime =
					date + new SimpleDateFormat("HHmmss").format(new java.util.Date());
			crawlDir = new File(jobDir + "/" + conf.getName() + "/" + datetime);
			if (!crawlDir.exists()) {
				// create job directory
				WebgatherLogger.debug("Create job Directory " + jobDir + "/"
						+ conf.getName() + "/" + datetime);
				crawlDir.mkdirs();
			}
		} catch (Exception e) {
			msg = "Cannot create jobDir in " + jobDir + "/" + conf.getName();
			WebgatherLogger.error(msg);
			throw new RuntimeException(msg);
		}
	}

	/**
	 * starts crawling with wpull
	 */
	public void startJob() {
		try {
			String executeCommand = buildExecCommand();
			WebgatherLogger.info("Executing command " + executeCommand);
			WebgatherLogger.info("Logfile = " + crawlDir.toString() + "/crawl.log");
			String[] execArr = executeCommand.split(" ");
			ProcessBuilder pb = new ProcessBuilder(execArr);
			assert crawlDir.isDirectory();
			pb.directory(crawlDir);
			File log = new File(crawlDir.toString() + "/crawl.log");
			log.createNewFile();
			pb.redirectErrorStream(true);
			pb.redirectOutput(ProcessBuilder.Redirect.appendTo(log));
			Process proc = pb.start();
			assert pb.redirectInput() == ProcessBuilder.Redirect.PIPE;
			assert pb.redirectOutput().file() == log;
			assert proc.getInputStream().read() == -1;
			/*
			 * den Pfad zum WARC unter Globals.heritrixData zu hängen ist eigentlich
			 * Blödsinn, aber ohne localpath wird im Frontend kein Link zu Openwayback
			 * erzeugt (warum nicht ?)
			 */
			localpath = Globals.heritrixData + "/wpull-data" + "/" + conf.getName()
					+ "/" + datetime + "/" + warcFilename + ".warc.gz";
			// exitState = proc.waitFor(); // don't wait
		} catch (Exception e) {
			WebgatherLogger.error(e.toString());
			throw new RuntimeException("wpull crawl not successfully started!", e);
		}
	}

	/**
	 * Builds a shell executable command which starts a wpull crawl
	 * 
	 * @return the shell executable command as a String
	 */
	private String buildExecCommand() {
		String urlRaw = conf.getUrl().replaceAll("^http://", "")
				.replaceAll("^https://", "").replaceAll("/$", "");
		warcFilename = "WEB-" + urlRaw + "-" + date;
		StringBuilder sb = new StringBuilder();
		sb.append(crawler + " " + conf.getUrl());
		ArrayList<String> domains = conf.getDomains();
		if (domains.size() > 0) {
			sb.append(" --span-hosts");
		}
		sb.append(" --domains=" + urlRaw);
		for (int i = 0; i < domains.size(); i++) {
			sb.append("," + domains.get(i));
		}
		sb.append(" --recursive");
		ArrayList<String> urlsExcluded = conf.getUrlsExcluded();
		if (urlsExcluded.size() > 0) {
			sb.append(" --reject-regex=.*" + urlsExcluded.get(0));
			for (int i = 1; i < urlsExcluded.size(); i++) {
				sb.append("|" + urlsExcluded.get(i));
			}
			sb.append(".*");
		}
		sb.append(" --link-extractors=javascript,html,css");
		sb.append(" --warc-file=" + warcFilename);
		sb.append(" --user-agent=\"InconspiciousWebBrowser/1.0\" --no-robots");
		sb.append(" --escaped-fragment --strip-session-id");
		sb.append(
				" --no-host-directories --convert-links --page-requisites --no-parent");
		sb.append(" --database=" + warcFilename + ".db");
		sb.append(" --no-check-certificate --no-directories");
		return sb.toString();
	}

	/**
	 * Ermittelt Crawler Exit Status des letzten Crawls
	 * 
	 * @param node der Knoten einer Webpage
	 * @return Crawler Exit Status des letzten wpull-Crawls
	 */
	public static int getCrawlExitStatus(Node node) {
		File latestCrawlDir = Webgatherer.getLatestCrawlDir(
				Play.application().configuration().getString("regal-api.wpull.jobDir"),
				node.getPid());
		if (latestCrawlDir == null) {
			WebgatherLogger.warn("Letztes Crawl-Verzeichnis zu Webpage "
					+ node.getName() + " kann nicht gefunden werden!");
			return -3;
		}
		File crawlLog = new File(latestCrawlDir.toString() + "/crawl.log");
		if (!crawlLog.exists()) {
			WebgatherLogger.warn("Crawl-Verzeichnis " + latestCrawlDir.toString()
					+ " existiert, aber darin kein crawl.log.");
			return -2;
		}
		/* das Log wird geparst */
		int exitStatus = -1;
		BufferedReader buf = null;
		String regExp = "^INFO Exiting with status ([0-9]+)";
		Pattern pattern = Pattern.compile(regExp);
		try {
			buf = new BufferedReader(new FileReader(crawlLog));
			String line = null;
			while ((line = buf.readLine()) != null) {
				Matcher matcher = pattern.matcher(line);
				if (matcher.find()) {
					exitStatus = Integer.parseInt(matcher.group(1));
					break;
				}
			}
		} catch (IOException e) {
			WebgatherLogger
					.warn("Crawler Exit Status cannot be defered from crawlLog "
							+ crawlLog.getAbsolutePath() + "!", e.toString());
		} finally {
			try {
				if (buf != null) {
					buf.close();
				}
			} catch (IOException e) {
				WebgatherLogger.warn("Read Buffer cannot be closed!");
			}
		}
		return exitStatus;
	}

	/**
	 * Ermittelt den aktuellen Status des zuletzt gestarteten Crawls
	 * 
	 * @param node der Knoten einer Webpage
	 * @return Crawler Status des zuletzt gestarteten wpull-Crawls; mögliche
	 *         Werte: NEW - RUNNING - PAUSED (nur Heritrix) - ABORTED (beendet vom
	 *         Operator) - CRASHED - FINISHED
	 */
	public static CrawlControllerState getCrawlControllerState(Node node) {
		// 1. Kein Crawl-Verzeichnis mit crawl.log vorhanden => Status = NEW
		File latestCrawlDir = Webgatherer.getLatestCrawlDir(
				Play.application().configuration().getString("regal-api.wpull.jobDir"),
				node.getPid());
		if (latestCrawlDir == null) {
			WebgatherLogger.info("Letztes Crawl-Verzeichnis zu Webpage "
					+ node.getName() + " kann nicht gefunden werden!");
			return CrawlControllerState.NEW;
		}
		File crawlLog = new File(latestCrawlDir.toString() + "/crawl.log");
		if (!crawlLog.exists()) {
			WebgatherLogger.info("Crawl-Verzeichnis " + latestCrawlDir.toString()
					+ " existiert, aber darin kein crawl.log.");
			return CrawlControllerState.NEW;
		}
		// 2. Läuft noch => Status = RUNNING
		if (isWpullCrawlRunning(node)) {
			return CrawlControllerState.RUNNING;
		}
		// 3. Läuft nicht mehr.
		/* das Log wird geparst */
		BufferedReader buf = null;
		String regExp = "^INFO FINISHED.";
		Pattern pattern = Pattern.compile(regExp);
		try {
			buf = new BufferedReader(new FileReader(crawlLog));
			String line = null;
			while ((line = buf.readLine()) != null) {
				Matcher matcher = pattern.matcher(line);
				if (matcher.find()) {
					return CrawlControllerState.FINISHED;
				}
			}
		} catch (IOException e) {
			WebgatherLogger.warn(
					"Crawl Controller State cannot be defered from crawlLog "
							+ crawlLog.getAbsolutePath() + "! Assuming CRASHED.",
					e.toString());
		} finally {
			try {
				if (buf != null) {
					buf.close();
				}
			} catch (IOException e) {
				WebgatherLogger.warn("Read Buffer cannot be closed!");
			}
		}
		return CrawlControllerState.CRASHED;
	}

	/**
	 * Prüfung, ob ein Crawl zu einer gegebenen URL aktuell läuft
	 * 
	 * @param node der Knoten zu der Webpage mit der URL
	 * @return boolean Crawl läuft
	 */
	public static boolean isWpullCrawlRunning(Node node) {
		BufferedReader buf = null;
		String cmd = "ps -eaf";
		String regExp1 =
				Play.application().configuration().getString("regal-api.wpull.crawler");
		Pattern pattern1 = Pattern.compile(regExp1);
		Matcher matcher1 = null;
		try {
			String urlRaw =
					Gatherconf.create(node.getConf()).getUrl().replaceAll("^http://", "")
							.replaceAll("^https://", "").replaceAll("/$", "");
			String regExp2 = "domains=" + urlRaw;
			Pattern pattern2 = Pattern.compile(regExp2);
			Matcher matcher2 = null;
			WebgatherLogger.debug("Setze Systemkommando ab: " + cmd);
			WebgatherLogger.debug("Suche nach URL_RAW in wpull-Aufrufen: " + urlRaw);
			String line;
			Process proc = Runtime.getRuntime().exec(cmd);
			buf = new BufferedReader(new InputStreamReader(proc.getInputStream()));
			while ((line = buf.readLine()) != null) {
				matcher1 = pattern1.matcher(line);
				if (matcher1.find()) {
					matcher2 = pattern2.matcher(line);
					if (matcher2.find()) {
						WebgatherLogger
								.debug("Found wpull Crawl process for this url=" + line);
						return true;
					}
				}
			}
		} catch (Exception e) {
			WebgatherLogger.warn("Fehler beim Aufruf des Systenkommandos: " + cmd,
					e.toString());
			throw new RuntimeException(
					"Crawl Job Zustand kann nicht bestimmt werden !", e);
		} finally {
			try {
				if (buf != null) {
					buf.close();
				}
			} catch (IOException e) {
				WebgatherLogger.warn("Read Buffer cannot be closed!");
			}
		}
		return false;
	}

}
