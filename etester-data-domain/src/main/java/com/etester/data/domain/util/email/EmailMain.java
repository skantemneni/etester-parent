package com.etester.data.domain.util.email;

import java.util.Locale;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;


public class EmailMain {
    public static void main(String[] args) {
        ApplicationContext context = new ClassPathXmlApplicationContext("beans-jdbc-test.xml");

        WebuserNotifier webuserNotifier = (WebuserNotifier) context.getBean("emailNotifierService");
        webuserNotifier.notifyNewWebuser("Sesi Kantemneni", "sesi.kantemneni@gmail.com", "sesi.kantemneni@gmail.com", "magicLink", Locale.ENGLISH);
    }
}
