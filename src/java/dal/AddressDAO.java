/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package dal;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import model.Address;

/**
 *
 * @author GiGaByte
 */
public class AddressDAO extends DBContext {

    /**
     * Lấy danh sách địa chỉ của một account dựa vào accountId.
     * Sắp xếp để địa chỉ mặc định (is_default = 1) luôn nổi lên đầu tiên.
     */
    public List<Address> getAddressesByAccountId(int accountId) {
        List<Address> list = new ArrayList<>();
        // Câu lệnh SQL: ORDER BY is_default DESC giúp địa chỉ mặc định luôn ở đầu danh sách
        String sql = "SELECT address_id, account_id, receiver_name, receiver_phone, full_address, is_default "
                   + "FROM tbAddress "
                   + "WHERE account_id = ? "
                   + "ORDER BY is_default DESC, address_id ASC";
        
        try {
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setInt(1, accountId);
            ResultSet rs = ps.executeQuery();
            
            while (rs.next()) {
                Address addr = new Address();
                addr.setAddressId(rs.getInt("address_id"));
                addr.setAccountId(rs.getInt("account_id"));
                addr.setReceiverName(rs.getString("receiver_name"));
                addr.setReceiverPhone(rs.getString("receiver_phone"));
                addr.setFullAddress(rs.getString("full_address"));
                
                // Trong SQL Server kiểu BIT trả về boolean trong Java
                addr.setDefault(rs.getBoolean("is_default")); 
                
                list.add(addr);
            }
        } catch (SQLException ex) {
            Logger.getLogger(AddressDAO.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return list;
    }
    public Address getAnAddressesByAccountId(int accountId) {
       
        // Câu lệnh SQL: ORDER BY is_default DESC giúp địa chỉ mặc định luôn ở đầu danh sách
        String sql = "SELECT address_id, account_id, receiver_name, receiver_phone, full_address, is_default "
                   + "FROM tbAddress "
                   + "WHERE account_id = ? and is_default = 1 "
                   + "ORDER BY is_default DESC, address_id ASC";
        
        try {
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setInt(1, accountId);
            ResultSet rs = ps.executeQuery();
            
            while (rs.next()) {
                Address addr = new Address();
                addr.setAddressId(rs.getInt("address_id"));
                addr.setAccountId(rs.getInt("account_id"));
                addr.setReceiverName(rs.getString("receiver_name"));
                addr.setReceiverPhone(rs.getString("receiver_phone"));
                addr.setFullAddress(rs.getString("full_address"));
                
                // Trong SQL Server kiểu BIT trả về boolean trong Java
                addr.setDefault(rs.getBoolean("is_default")); 
                
                return addr;
            }

            // DỰ PHÒNG: nếu vì lý do gì đó không có địa chỉ nào is_default = 1
            // (dữ liệu cũ, hoặc account tạo trước khi có fix tự-đặt-mặc-định ở
            // insertAddress()), đừng trả về null khiến Checkout bó tay hoàn
            // toàn — lấy tạm địa chỉ được thêm gần nhất để khách vẫn đặt hàng
            // được, còn hơn là chặn đứng cả luồng mua hàng.
            String fallbackSql = "SELECT TOP 1 address_id, account_id, receiver_name, receiver_phone, full_address, is_default "
                               + "FROM tbAddress WHERE account_id = ? ORDER BY address_id DESC";
            try (PreparedStatement psFallback = connection.prepareStatement(fallbackSql)) {
                psFallback.setInt(1, accountId);
                try (ResultSet rsFallback = psFallback.executeQuery()) {
                    if (rsFallback.next()) {
                        Address addr = new Address();
                        addr.setAddressId(rsFallback.getInt("address_id"));
                        addr.setAccountId(rsFallback.getInt("account_id"));
                        addr.setReceiverName(rsFallback.getString("receiver_name"));
                        addr.setReceiverPhone(rsFallback.getString("receiver_phone"));
                        addr.setFullAddress(rsFallback.getString("full_address"));
                        addr.setDefault(rsFallback.getBoolean("is_default"));
                        return addr;
                    }
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(AddressDAO.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return null;
    }
    public void deleteAddress(int addressId) {
    String sql = "DELETE FROM [tbAddress] WHERE address_id = ?";
    try {
        PreparedStatement st = connection.prepareStatement(sql);
        st.setInt(1, addressId);
        st.executeUpdate();
    } catch (Exception e) {
        System.out.println("Lỗi deleteAddress: " + e.getMessage());
    }
}

// 2. Hàm set mặc định
public void setDefaultAddress(int accountId, int addressId) {
    try {
        // Bước A: Reset toàn bộ địa chỉ của user này về KHÔNG mặc định (0 hoặc false)
        String sqlReset = "UPDATE [tbAddress] SET is_default = 0 WHERE account_id = ?";
        PreparedStatement stReset = connection.prepareStatement(sqlReset);
        stReset.setInt(1, accountId);
        stReset.executeUpdate();

        // Bước B: Cập nhật riêng địa chỉ được chọn thành Mặc định (1 hoặc true)
        String sqlSet = "UPDATE [tbAddress] SET is_default = 1 WHERE address_id = ?";
        PreparedStatement stSet = connection.prepareStatement(sqlSet);
        stSet.setInt(1, addressId);
        stSet.executeUpdate();
        
    } catch (Exception e) {
        System.out.println("Lỗi setDefaultAddress: " + e.getMessage());
    }
}
public boolean insertAddress(String name,String phone,String address,String accountid) {
    // TRƯỚC ĐÂY: luôn insert is_default = 0 (hardcode), bất kể đây có phải địa
    // chỉ ĐẦU TIÊN của tài khoản hay không. Hậu quả: khách mới, chưa có địa
    // chỉ nào, thêm địa chỉ xong vẫn không có địa chỉ nào is_default = 1 ->
    // Checkout (chỉ lấy is_default = 1) không hiện được gì, kể cả sau khi vừa
    // thêm xong — trừ khi khách tự vào Account bấm "Đặt làm mặc định" (bước ẩn,
    // không ai biết). Giờ tự động đặt mặc định nếu đây là địa chỉ đầu tiên.
    boolean isFirstAddress = false;
    String sqlCount = "SELECT COUNT(*) FROM tbAddress WHERE account_id = ?";
    try (PreparedStatement psCount = connection.prepareStatement(sqlCount)) {
        psCount.setString(1, accountid);
        try (ResultSet rs = psCount.executeQuery()) {
            if (rs.next() && rs.getInt(1) == 0) {
                isFirstAddress = true;
            }
        }
    } catch (Exception e) {
        System.out.println("Lỗi đếm địa chỉ: " + e.getMessage());
    }

    String sql = "INSERT INTO tbAddress ( receiver_name, receiver_phone, full_address, is_default, account_id) "
               + "VALUES ( ?, ?, ?,?,?)";
    try {
        PreparedStatement st = connection.prepareStatement(sql);
        st.setString(1, name);
        st.setString(2, phone);
        st.setString(3, address);
        st.setInt(4, isFirstAddress ? 1 : 0);
         st.setString(5, accountid);
        int rows = st.executeUpdate();
        return rows > 0;
    } catch (Exception e) {
        System.out.println("Lỗi insertAddress: " + e.getMessage());
        return false;
    }
}
    // 3. Hàm cập nhật (sửa) một địa chỉ đã có — TRƯỚC ĐÂY chưa hề tồn tại,
// khiến nút "Sửa" trên giao diện Account.jsp không có gì để gọi cả.
public boolean updateAddress(int addressId, String name, String phone, String address) {
    String sql = "UPDATE tbAddress SET receiver_name = ?, receiver_phone = ?, full_address = ? "
               + "WHERE address_id = ?";
    try (PreparedStatement st = connection.prepareStatement(sql)) {
        st.setString(1, name);
        st.setString(2, phone);
        st.setString(3, address);
        st.setInt(4, addressId);
        int rows = st.executeUpdate();
        return rows > 0;
    } catch (Exception e) {
        System.out.println("Lỗi updateAddress: " + e.getMessage());
        return false;
    }
}

}