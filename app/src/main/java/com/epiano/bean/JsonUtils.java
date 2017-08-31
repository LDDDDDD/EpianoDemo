package com.epiano.bean;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.Iterator;
import java.util.List;

public class JsonUtils {


    public List<Student> parseMovieTimeFromJson(String jsonData){
        Type listType = new TypeToken<List<Student>>() {}.getType();
        Gson gson = new Gson();
        List<Student> students = gson.fromJson(jsonData, listType);
        Iterator<Student> iterator = students.iterator();
        while (iterator.hasNext()){
            Student student = (Student)iterator.next();
//            System.out.println("StudentUserId--->" + student.getStudentUserId());
//            System.out.println("UserName--->" + student.getUserName());
//            System.out.println("图片ID："+student.getPortraitId());
//            System.out.println("电话："+student.getPhoneNum());
        }
        return students;
    }


    public List<Song> parseSongFromJson(String jsonData){
        Type listType = new TypeToken<List<Song>>() {}.getType();
        Gson gson = new Gson();
        List<Song> songs = gson.fromJson(jsonData, listType);
        Iterator<Song> iterator = songs.iterator();
        while (iterator.hasNext()){
            Song song = (Song)iterator.next();
//            System.out.println("songID--->" + song.getIdx());
//            System.out.println("songName--->" + song.getSongname());
//            System.out.println("Wirter："+song.getWriter());
//            System.out.println("pyname："+song.getPyname());
        }
        return songs;
    }

    public List<Lesson> parseLessonFromJson(String jsonData){
        Type listType = new TypeToken<List<Lesson>>() {}.getType();
        Gson gson = new Gson();
        List<Lesson> lessons = gson.fromJson(jsonData, listType);
        Iterator<Lesson> iterator = lessons.iterator();
        while (iterator.hasNext()){
            Lesson song = (Lesson)iterator.next();
            System.out.println("Lesson--->" + song.getUserName().toString());
            System.out.println("Lesson--->" + song.getEstablishTime().toString());
            System.out.println("Lesson--->" + song.getLastActTime().toString());
        }
        return lessons;
    }

}   
