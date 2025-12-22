package com.example.jigsawsolver.data

import androidx.room.TypeConverter

class Converters {
    @TypeConverter
    fun fromEdgeType(value: EdgeType): String {
        return value.name
    }
    
    @TypeConverter
    fun toEdgeType(value: String): EdgeType {
        return EdgeType.valueOf(value)
    }
    
    @TypeConverter
    fun fromEdgeFeature(value: EdgeFeature): String {
        return value.name
    }
    
    @TypeConverter
    fun toEdgeFeature(value: String): EdgeFeature {
        return EdgeFeature.valueOf(value)
    }
}
