package com.epiano.bean;

import java.util.ArrayList;
import java.util.List;

public class LessonDAO {

	public static List<Lesson> getLessonsByYearAndMonth(int year, int month) {
		List<Lesson> lessons = new ArrayList<Lesson>();

		Lesson lesson = new Lesson();
		lesson.setId(5);
		lesson.setUserName("语文");
		lesson.setNum(2);
		lesson.setStatus(1);
		lesson.setLessonDate("2017-08-14 12:00");
		lesson.setEstablishTime("12:00");
		lesson.setLastActTime("13:00");
		lessons.add(lesson);

		Lesson lesson1 = new Lesson();
		lesson1.setId(2);
		lesson1.setUserName("数学");
		lesson1.setNum(2);
		lesson1.setStatus(0);
		lesson1.setLessonDate("2017-08-14 15:00");
		lesson1.setEstablishTime("15:00");
		lesson1.setLastActTime("16:00");
		lessons.add(lesson1);

		Lesson lesson2 = new Lesson();
		lesson2.setId(5);
		lesson2.setUserName("英语");
		lesson2.setNum(3);
		lesson2.setStatus(3);
		lesson2.setLessonDate("2017-08-14 08:00");
		lesson2.setEstablishTime("08:00");
		lesson2.setLastActTime("10:00");
		lessons.add(lesson2);
		return lessons;
	}




//	public static List<Lesson> getLessonsByDate(String string, String nextDay) {
//		List<Lesson> lessons = new ArrayList<Lesson>();
//
//		Lesson lesson = new Lesson();
//		lesson.setId(1);
//		lesson.setName("语文");
//		lesson.setNum(1);
//		lesson.setStatus(1);
//		lesson.setLessonDate("2017-08-14 12:00");
//		lesson.setsTime("12:00");
//		lesson.seteTime("13:00");
//		lessons.add(lesson);
//
//		Lesson lesson1 = new Lesson();
//		lesson1.setId(2);
//		lesson1.setName("数学");
//		lesson1.setNum(2);
//		lesson1.setStatus(0);
//		lesson1.setLessonDate("2017-08-14 15:00");
//		lesson1.setsTime("15:00");
//		lesson1.seteTime("16:00");
//		lessons.add(lesson1);
//
//		Lesson lesson2 = new Lesson();
//		lesson2.setId(2);
//		lesson2.setName("英语");
//		lesson2.setNum(3);
//		lesson2.setStatus(1);
//		lesson2.setLessonDate("2017-08-14 08:00");
//		lesson2.setsTime("08:00");
//		lesson2.seteTime("09:00");
//		lessons.add(lesson2);
//		return lessons;
//	}

}
