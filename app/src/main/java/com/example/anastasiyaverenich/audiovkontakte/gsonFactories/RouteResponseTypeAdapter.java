package com.example.anastasiyaverenich.audiovkontakte.gsonFactories;

import com.example.anastasiyaverenich.audiovkontakte.modules.Audio;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class RouteResponseTypeAdapter extends TypeAdapter<Audio> {

    private final TypeAdapter<JsonElement> jsonElementTypeAdapter;
    private final TypeAdapter<Audio> recipeTypeAdapter;
    private final TypeAdapter<Audio.Track> feedTypeAdapter;

    public RouteResponseTypeAdapter(Gson gson) {
        this.jsonElementTypeAdapter = gson.getAdapter(JsonElement.class);
        this.recipeTypeAdapter = gson.getAdapter(Audio.class);
        this.feedTypeAdapter = gson.getAdapter(Audio.Track.class);
    }

    @Override
    public void write(JsonWriter out, Audio value) throws IOException {
        recipeTypeAdapter.write(out,value);
    }

    @Override
    public Audio read(JsonReader jsonReader) throws IOException {
        Audio result = new Audio();
        List<Audio.Track> feeds = new ArrayList<>();
        result.response = feeds;
        if (jsonReader.peek() == JsonToken.BEGIN_OBJECT) {
            JsonObject responseObject = (JsonObject) jsonElementTypeAdapter.read(jsonReader);
            JsonArray response = responseObject.getAsJsonArray("response");
            if (response != null) {
                for (JsonElement element: response) {
                    if(!element.isJsonPrimitive()){
                        feeds.add(feedTypeAdapter.fromJsonTree(element));
                    }
                }
            }
        }
        return result;
    }
}