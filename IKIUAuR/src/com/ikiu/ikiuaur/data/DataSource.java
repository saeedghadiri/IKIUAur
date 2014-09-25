package com.ikiu.ikiuaur.data;

import java.util.List;

import android.content.Context;

import com.ikiu.ikiuaur.ui.Marker;

/**
 * This abstract class should be extended for new data sources.
 * 
 */
public abstract class DataSource {

    public abstract List<Marker> getMarkers();
}
