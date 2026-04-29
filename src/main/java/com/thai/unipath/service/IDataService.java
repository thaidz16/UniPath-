package com.thai.unipath.service;

import com.thai.unipath.model.ThiSinh;
import java.util.List;

public interface IDataService {
    // Đọc danh sách thí sinh từ nguồn dữ liệu
    List<ThiSinh> readData(String filePath);

    // Tìm kiếm thí sinh theo SBD
    ThiSinh searchBySBD(List<ThiSinh> list, String sbd);

    void writeData(List<ThiSinh> list, String filePath);
}
