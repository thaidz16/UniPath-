package com.thai.unipath.model;

import java.time.LocalDate;

public abstract class Nguoi {
    protected String hoTen;
    protected LocalDate ngaySinh;
    protected String queQuan;

    public Nguoi() {}

    public Nguoi(String hoTen, LocalDate ngaySinh, String queQuan) {
        this.hoTen = hoTen;
        this.ngaySinh = ngaySinh;
        this.queQuan = queQuan;
    }

    // Getter và Setter
    public String getHoTen() { return hoTen; }
    public void setHoTen(String hoTen) { this.hoTen = hoTen; }

    public LocalDate getNgaySinh() { return ngaySinh; }
    public void setNgaySinh(LocalDate ngaySinh) { this.ngaySinh = ngaySinh; }

    public String getQueQuan() { return queQuan; }
    public void setQueQuan(String queQuan) { this.queQuan = queQuan; }
}
