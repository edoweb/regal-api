
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
import static play.mvc.Results.notFound;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import helper.DevLoggerContext;
import helper.oai.OaiDispatcher;
import models.Globals;
import play.Application;
import play.GlobalSettings;
import play.Play;
import play.libs.F.Promise;
import play.mvc.Action;
import play.mvc.Http.Request;
import play.mvc.Http.RequestHeader;
import play.mvc.Result;

/**
 * @author Jan Schnasse
 *
 */
public class Global extends GlobalSettings {
	public void onStart(Application app) {
		try {
			if (play.api.Play.isProd(play.api.Play.current())) {
				play.Logger.info("Regal-API started!");
				for (int i = 0; i < Globals.namespaces.length; i++) {

					play.Logger
							.info("Init fedora content models for " + Globals.namespaces[i]);
					OaiDispatcher.initContentModels(Globals.namespaces[i]);
				}
				play.Logger.info("Init fedora content models for default namespace");
				OaiDispatcher.initContentModels("");

				Globals.search.init(Globals.namespaces);
			} else {
				// play Framework läuft im Test- oder Entwicklungsmodus
				// Logger-Konfiguration für Entwickler/innen laden
				DevLoggerContext.doConfigure(Play.application().configuration()
						.getString("logger.config.developer"));
			}
			Globals.taskManager.init();
			Globals.taskManager.execute();
		} catch (Throwable t) {
			t.printStackTrace();
		}
	}

	@Override
	public void onStop(Application app) {
		// Globals.profile.saveMap();
		play.Logger.info("Application shutdown...");
		Globals.taskManager.shutdown();
	}

	public Promise<Result> onHandlerNotFound(RequestHeader request) {
		return Promise.<Result> pure(notFound("Action not found " + request.uri()));
	}

	@SuppressWarnings("rawtypes")
	public Action onRequest(Request request, Method actionMethod) {
		play.Logger.info("\n" + request.toString() + "\n\t"
				+ mapToString(request.headers()) + "\n\t" + request.body().toString());
		return super.onRequest(request, actionMethod);
	}

	private String mapToString(Map<String, String[]> map) {
		StringBuilder sb = new StringBuilder();
		Iterator<Entry<String, String[]>> iter = map.entrySet().iterator();
		while (iter.hasNext()) {
			Entry<String, String[]> entry = iter.next();
			sb.append(entry.getKey());
			sb.append('=').append('"');
			sb.append(Arrays.toString(entry.getValue()));
			sb.append('"');
			if (iter.hasNext()) {
				sb.append("\n\t'");
			}
		}
		return sb.toString();

	}
}
