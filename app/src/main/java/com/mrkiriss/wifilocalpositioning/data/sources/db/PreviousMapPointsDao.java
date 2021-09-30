package com.mrkiriss.wifilocalpositioning.data.sources.db;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import com.mrkiriss.wifilocalpositioning.data.models.search.PreviousNameInput;

import java.util.List;

@Dao
public interface PreviousMapPointsDao {

    @Query("SELECT * FROM previousnameinput")
    List<PreviousNameInput> findAll();

    @Query("SELECT * FROM previousnameinput WHERE inputName = :inputName")
    PreviousNameInput findByInputName(String inputName);

    @Insert
    void insert(PreviousNameInput previousNameInput);

    @Delete
    void delete(PreviousNameInput previousNameInput);

    @Query("DELETE FROM previousnameinput WHERE inputName = :inputName")
    void deleteByInputName(String inputName);
}
