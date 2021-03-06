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

import java.io.IOException;
import java.io.StringReader;
import java.text.SimpleDateFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.supercsv.cellprocessor.Optional;
import org.supercsv.cellprocessor.constraint.UniqueHashCode;
import org.supercsv.cellprocessor.ift.CellProcessor;
import org.supercsv.io.CsvMapReader;
import org.supercsv.io.ICsvMapReader;
import org.supercsv.prefs.CsvPreference;

import com.ibm.icu.util.Calendar;

import models.Gatherconf;
import play.Logger;

/**
 * @author Jan Schnasse
 *
 */
public class GatherconfImporter {

	private static final Logger.ALogger WebgatherLogger =
			Logger.of("webgatherer");

	private static final CsvPreference PIPE_DELIMITED =
			new CsvPreference.Builder('"', '|', "\n").build();

	/**
	 * @param csv a csv export of old digitool gatherconf
	 * @return a List of Gatherconfigs for regal
	 */
	public static List<Gatherconf> read(String csv, int firstId) {
		try (ICsvMapReader mapReader =
				new CsvMapReader(new StringReader(csv), PIPE_DELIMITED)) {
			Calendar cal = Calendar.getInstance();
			List<Gatherconf> result = new ArrayList<Gatherconf>();
			final String[] header = mapReader.getHeader(true);
			final CellProcessor[] processors = getProcessors();
			Map<String, Object> row;
			int id = firstId;
			while ((row = mapReader.read(header, processors)) != null) {
				WebgatherLogger.info("read row " + row.toString());
				cal.add(Calendar.HOUR, 2);
				String startDateStr = (String) row.get("ENTRY_DATE");
				Date startDate =
						new SimpleDateFormat("dd.MM.yyyy HH:mm:ss").parse(startDateStr);
				play.Logger.debug("Add new Webpage with startdate: "
						+ new SimpleDateFormat("yyyy-MM-dd-HH:mm:ss").format(startDate));
				Gatherconf conf = new Gatherconf();
				conf.setId("" + id++);
				conf.setStartDate(startDate);
				String levels = (String) row.get("EBENEN");
				if ("null".equals(levels)) {
					conf.setDeepness(-1);
				} else {
					conf.setDeepness(Integer.parseInt(levels));
				}
				String intervallString = (String) row.get("INTERVALL_H");
				if ("null".equals(intervallString)) {
					conf.setInterval(Gatherconf.Interval.annually);
				} else {
					int intervall = Integer.parseInt(intervallString);
					if (intervall <= 4320) {
						conf.setInterval(Gatherconf.Interval.halfYearly);
					} else {
						conf.setInterval(Gatherconf.Interval.annually);
					}
				}
				String robots = (String) row.get("IGNORE_ROBOTS");
				if (robots != null) {
					play.Logger.warn("Robots for " + conf.getUrl() + " is " + robots);
				}
				conf.setRobotsPolicy(Gatherconf.RobotsPolicy.classic);
				conf.setUrl((String) row.get("URL"));
				conf.setName((String) row.get("ALEPHIDN"));
				String webSchnitt = (String) row.get("WEBSCHNITT");
				if (webSchnitt.equals("N")) {
					conf.setInterval(Gatherconf.Interval.once);
				}
				String usrStat = (String) row.get("USR_STAT");
				if (usrStat.equals("DA")) {
					conf.setActive(false);
				}

				result.add(conf);
			}
			return result;
		} catch (IOException e) {
			throw new HttpArchiveException(500, e);
		} catch (ParseException pe) {
			WebgatherLogger.warn(
					"Parsing of Gatherconf list failed. Perhaps a wrong date format.");
			throw new RuntimeException(pe);
		}
	}

	/**
	 * ID|URL|ALEPHIDN|WEBSCHNITT|INTERVALL_H|EBENEN|GATHER_DIR|LAST_ENTRY|
	 * LAST_GATHER|LAST_INGEST|TOLOAD_DIR|STATUS|LAST_PID|USR_STAT|ENTRY_DATE|
	 * EXTENDED_TEXT|IGNORE_ROBOTS|DEACT_MSG
	 * 
	 * @return
	 */
	private static CellProcessor[] getProcessors() {
		final CellProcessor[] processors =
				new CellProcessor[] { new UniqueHashCode(), // ID
						new Optional(), // URL
						new Optional(), // ALEPHIDN
						new Optional(), // WEBSCHNITT
						new Optional(), // INTERVALL_H
						new Optional(), // EBENEN
						new Optional(), // GATHER_DIR
						new Optional(), // LAST_ENTRY
						new Optional(), // LAST_GATHER
						new Optional(), // LAST_INGEST
						new Optional(), // TOLOAD_DIR
						new Optional(), // STATUS
						new Optional(), // LAST_PID
						new Optional(), // USR_STAT
						new Optional(), // ENTRY_DATE
						new Optional(), // EXTENDED_TEXT
						new Optional(), // IGNORE_ROBOTS
						new Optional() // DEACT_MSG
				}; // EMPTY
		return processors;
	}
}
