package com.simiacryptus.lang;

import org.json.JSONException;
import org.json.JSONObject;

public interface JsonFormattable
{
  
  public abstract JSONObject toJson() throws JSONException;
  
}