package com.example.petreg;

import java.util.List;

public class Pet {
    private long id;
    private String name;
    private int birth;
    private String fio;
    private String address;
    private String tel;
    private List<Vaccination> vaccinations;

    public static class Vaccination{
        private String name;
        private String date;

        public Vaccination(){}

        public Vaccination(String name, String date) {
            this.name = name;
            this.date = date;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getDate() {
            return date;
        }

        public void setDate(String date) {
            this.date = date;
        }
    }

    public Pet(){}

    public Pet(long id, String name, int age, String fio, String address, String tel) {
        this.id = id;
        this.name = name;
        this.birth = age;
        this.fio = fio;
        this.address = address;
        this.tel = tel;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getBirth() {
        return birth;
    }

    public void setBirth(int birth) {
        this.birth = birth;
    }

    public String getFio() {
        return fio;
    }

    public void setFio(String fio) {
        this.fio = fio;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getTel() {
        return tel;
    }

    public void setTel(String tel) {
        this.tel = tel;
    }

    @Override
    public String toString() {
        return "Pet{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", birth=" + birth +
                ", fio='" + fio + '\'' +
                ", address='" + address + '\'' +
                ", tel='" + tel + '\'' +
                ", vaccinations=" + vaccinations +
                '}';
    }

    public List<Vaccination> getVaccinations() {
        return vaccinations;
    }

    public void setVaccinations(List<Vaccination> vaccinations) {
        this.vaccinations = vaccinations;
    }
}
