package com.efficios.jabberwocky.views.timegraph.view.json;

import com.efficios.jabberwocky.views.timegraph.model.render.states.TimeGraphStateRender;
import com.google.common.base.Charsets;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public final class RenderToJson {

    private RenderToJson() {}

    private static final boolean PRETTY_PRINT = true;
    private static final int VERSION = 1;

    private static final String VERSION_KEY = "version"; //$NON-NLS-1$
    private static final String INTERVALS_KEY = "intervals"; //$NON-NLS-1$

    private static final String TREE_ELEMENT_KEY = "elem"; //$NON-NLS-1$
    private static final String START_TIME_KEY = "start"; //$NON-NLS-1$
    private static final String END_TIME_KEY = "end"; //$NON-NLS-1$
    private static final String STATE_NAME_KEY = "state"; //$NON-NLS-1$
    private static final String COLOR_KEY = "color"; //$NON-NLS-1$

    private static final Path OUTPUT_FILE = Paths.get("/home/alexandre/json-output"); //$NON-NLS-1$
    static {
        try {
            if (Files.exists(OUTPUT_FILE)) {
                Files.delete(OUTPUT_FILE);
            }
            Files.createFile(OUTPUT_FILE);
        } catch (IOException e) {
        }
    }

    private static final Gson GSON;
    static {
        if (PRETTY_PRINT) {
            GSON = new GsonBuilder().setPrettyPrinting().create();
        } else {
            GSON = new Gson();
        }
    }

    public static void printRenderTo2(List<TimeGraphStateRender> renders) {
        String json = GSON.toJson(renders);
        try (Writer bw = Files.newBufferedWriter(OUTPUT_FILE, Charsets.UTF_8)) {
            bw.write(json);
            bw.flush();
        } catch (IOException e1) {
        }
    }

    public static void printRenderToStdout(Object obj) {
        String json = GSON.toJson(obj);
        System.out.println(json);
    }

    // Alternative implementation using org.json
    public static void printRenderTo(List<TimeGraphStateRender> renders) {
        try (Writer bw = Files.newBufferedWriter(OUTPUT_FILE, Charsets.UTF_8)) {
            JSONObject root = new JSONObject();
            root.put(VERSION_KEY, VERSION);

            JSONArray intervalsRoot = new JSONArray();
            root.put(INTERVALS_KEY, intervalsRoot);

            renders.stream()
                    .flatMap(render -> render.getStateIntervals().stream())
                    .forEach(interval -> {
                        try {
                            JSONObject intervalObject = new JSONObject();
                            intervalObject.put(TREE_ELEMENT_KEY, interval.getStartEvent().getTreeElement().getName());
                            intervalObject.put(START_TIME_KEY, interval.getStartEvent().getTimestamp());
                            intervalObject.put(END_TIME_KEY, interval.getEndEvent().getTimestamp());
                            intervalObject.put(STATE_NAME_KEY, interval.getStateName());
                            intervalObject.put(COLOR_KEY, interval.getColorDefinition().toString());

                            intervalsRoot.put(intervalObject);
                        } catch (JSONException e) {
                                            /* Skip this interval */
                        }
                    });

            String json = (PRETTY_PRINT ? root.toString(1) : root.toString());
            bw.write(json);
            bw.flush();

        } catch (JSONException | IOException e) {
        }
    }

}
