package com.example.petreg;

public class Pet {
    private long id;
    private String name;
    private int age;
    private String fio;
    private String address;
    private String tel;

    public Pet(){}

    public Pet(long id, String name, int age, String fio, String address, String tel) {
        this.id = id;
        this.name = name;
        this.age = age;
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

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
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
                ", age=" + age +
                ", fio='" + fio + '\'' +
                ", address='" + address + '\'' +
                ", tel='" + tel + '\'' +
                '}';
    }
}
