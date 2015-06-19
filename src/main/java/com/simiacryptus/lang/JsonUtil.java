package com.simiacryptus.lang;

import org.json.JSONArray;
import org.json.JSONException;

public class JsonUtil
{

  public static JSONArray toJsonArray(Iterable<? extends JsonFormattable> objs) throws JSONException
  {
    JSONArray json = new JSONArray();
    for(JsonFormattable o : objs)
    {
      json.put(o.toJson());
    }
    return json;
  }
  
}
