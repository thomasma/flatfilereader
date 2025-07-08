package org.aver.fft;

import java.util.Date;

import org.aver.fft.annotations.Column;
import org.aver.fft.annotations.Transform;

/**
 * Bean for comma-separated values testing.
 */
@Transform(columnSeparator = ",")
public class CsvBean {
    private String name;
    private String email;
    private int age;
    private Date joinDate;

    @Column(position = 1, required = true)
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Column(position = 2, required = true)
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    @Column(position = 3, required = true)
    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    @Column(position = 4, required = true, format = "yyyy-MM-dd")
    public Date getJoinDate() {
        return joinDate;
    }

    public void setJoinDate(Date joinDate) {
        this.joinDate = joinDate;
    }
}