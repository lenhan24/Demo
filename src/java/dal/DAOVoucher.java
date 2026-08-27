/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package dal;

import java.util.ArrayList;
import java.util.List;
import model.VoucherDTO;
import java.sql.*;
/**
 *
 * @author GiGaByte
 */
public class DAOVoucher extends DBContext{

    /**
     * Lấy voucher account đã lưu (tbSavedVoucher) và đang HỢP LỆ:
     * status ACTIVE, trong hạn, đủ min_order_value, chưa vượt max_usage_limit
     * và per_customer_limit.
     */
    public List<VoucherDTO> getValidSavedVouchers(int accountId, long orderSubtotal) {
        List<VoucherDTO> list = new ArrayList<>();

        String sql = "SELECT v.voucher_id, v.code, v.discount_type, v.discount_value, "
                   + "v.max_discount_cap, v.min_order_value, v.start_date, v.end_date, "
                   + "v.max_usage_limit, v.per_customer_limit "
                   + "FROM tbVoucher v "
                   + "INNER JOIN tbSavedVoucher sv ON v.voucher_id = sv.voucher_id "
                   + "WHERE sv.account_id = ? "
                   + "AND v.status = 'ACTIVE' "
                   + "AND GETDATE() BETWEEN v.start_date AND v.end_date "
                   + "AND v.min_order_value <= ? "
                   + "AND (SELECT COUNT(*) FROM tbVoucherUsage vu WHERE vu.voucher_id = v.voucher_id) < v.max_usage_limit "
                   + "AND (SELECT COUNT(*) FROM tbVoucherUsage vu2 WHERE vu2.voucher_id = v.voucher_id AND vu2.account_id = ?) < v.per_customer_limit "
                   + "ORDER BY v.discount_value DESC";

    
        

        try {
           PreparedStatement stm = connection.prepareStatement(sql);
            stm.setInt(1, accountId);
            stm.setLong(2, orderSubtotal);
            stm.setInt(3, accountId);
            ResultSet rs = stm.executeQuery();

            while (rs.next()) {
                Long maxDiscountCap = null;
                long capRaw = rs.getLong("max_discount_cap");
                if (!rs.wasNull()) {
                    maxDiscountCap = capRaw;
                }

                VoucherDTO v = new VoucherDTO(
                        rs.getInt("voucher_id"),
                        rs.getString("code"),
                        rs.getString("discount_type"),
                        rs.getLong("discount_value"),
                        maxDiscountCap,
                        rs.getLong("min_order_value"),
                        rs.getTimestamp("start_date"),
                        rs.getTimestamp("end_date"),
                        rs.getInt("max_usage_limit"),
                        rs.getInt("per_customer_limit")
                );
                list.add(v);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            try {
//                if (rs != null) rs.close();
//                if (stm != null) stm.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return list;
    }
    /**
     * Lấy TẤT CẢ voucher đã lưu của account (không lọc theo còn hiệu lực hay
     * không) — dùng cho trang "Voucher đã lưu" trong hồ sơ, để khách xem lại
     * được cả voucher đã hết hạn/bị tắt, không chỉ voucher dùng được ngay.
     */
    public List<VoucherDTO> getSavedVouchersFull(int accountId) {
        List<VoucherDTO> list = new ArrayList<>();
        String sql = "SELECT v.voucher_id, v.code, v.discount_type, v.discount_value, "
                   + "v.max_discount_cap, v.min_order_value, v.start_date, v.end_date, "
                   + "v.max_usage_limit, v.per_customer_limit, v.status "
                   + "FROM tbVoucher v "
                   + "INNER JOIN tbSavedVoucher sv ON v.voucher_id = sv.voucher_id "
                   + "WHERE sv.account_id = ? "
                   + "ORDER BY sv.saved_at DESC";
        try (PreparedStatement stm = connection.prepareStatement(sql)) {
            stm.setInt(1, accountId);
            try (ResultSet rs = stm.executeQuery()) {
                while (rs.next()) {
                    Long maxDiscountCap = null;
                    long capRaw = rs.getLong("max_discount_cap");
                    if (!rs.wasNull()) {
                        maxDiscountCap = capRaw;
                    }
                    VoucherDTO v = new VoucherDTO(
                            rs.getInt("voucher_id"),
                            rs.getString("code"),
                            rs.getString("discount_type"),
                            rs.getLong("discount_value"),
                            maxDiscountCap,
                            rs.getLong("min_order_value"),
                            rs.getTimestamp("start_date"),
                            rs.getTimestamp("end_date"),
                            rs.getInt("max_usage_limit"),
                            rs.getInt("per_customer_limit"),
                            rs.getString("status")
                    );
                    list.add(v);
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return list;
    }

    public boolean saveVoucherForAccount(int accountId, int voucherId) {
    String sql = "INSERT INTO tbSavedVoucher (account_id, voucher_id, saved_at) VALUES (?, ?, GETDATE())";
    try (PreparedStatement stm = connection.prepareStatement(sql)) {
        stm.setInt(1, accountId);
        stm.setInt(2, voucherId);
        int rowsAffected = stm.executeUpdate();
        return rowsAffected > 0;
    } catch (Exception ex) {
        // Có thể bắt lỗi duplicate key (lỗi 2627 trong SQL Server) 
        // trong trường hợp user vô tình gọi API 2 lần
        System.err.println("Lỗi khi lưu voucher hoặc voucher đã được lưu trước đó.");
        ex.printStackTrace();
    }
    return false;
}
    public List<VoucherDTO> getAllActiveVouchers(int accountId) {
    List<VoucherDTO> list = new ArrayList<>();
    // Lấy các voucher đang ACTIVE, còn hạn, và check xem user đã lưu chưa
    String sql = "SELECT v.voucher_id, v.code, v.discount_type, v.discount_value, "
               + "v.max_discount_cap, v.min_order_value, v.start_date, v.end_date, "
               + "v.max_usage_limit, v.per_customer_limit, "
               + "CASE WHEN sv.voucher_id IS NOT NULL THEN 1 ELSE 0 END AS is_saved "
               + "FROM tbVoucher v "
               + "LEFT JOIN tbSavedVoucher sv ON v.voucher_id = sv.voucher_id AND sv.account_id = ? "
               + "WHERE v.status = 'ACTIVE' "
               + "AND v.end_date >= GETDATE() " 
               + "ORDER BY v.start_date DESC";

    try (PreparedStatement stm = connection.prepareStatement(sql)) {
        stm.setInt(1, accountId);
        try (ResultSet rs = stm.executeQuery()) {
            while (rs.next()) {
                Long maxDiscountCap = null;
                long capRaw = rs.getLong("max_discount_cap");
                if (!rs.wasNull()) {
                    maxDiscountCap = capRaw;
                }
                
                boolean isSaved = rs.getInt("is_saved") == 1;

                VoucherDTO v = new VoucherDTO(
                        rs.getInt("voucher_id"),
                        rs.getString("code"),
                        rs.getString("discount_type"),
                        rs.getLong("discount_value"),
                        maxDiscountCap,
                        rs.getLong("min_order_value"),
                        rs.getTimestamp("start_date"),
                        rs.getTimestamp("end_date"),
                        rs.getInt("max_usage_limit"),
                        rs.getInt("per_customer_limit")
                
                );
                list.add(v);
            }
        }
    } catch (Exception ex) {
        ex.printStackTrace();
    }
    return list;
}
    public VoucherDTO getVoucherByAccountAndId( int accountId, int voucherId, long orderSubtotal) {
    String sql = "SELECT v.voucher_id, v.code, v.discount_type, v.discount_value, "
               + "v.max_discount_cap, v.min_order_value, v.start_date, v.end_date, "
               + "v.max_usage_limit, v.per_customer_limit "
               + "FROM tbVoucher v "
               + "INNER JOIN tbSavedVoucher sv ON v.voucher_id = sv.voucher_id "
               + "WHERE sv.account_id = ? "
               + "AND v.voucher_id = ? "
               + "AND v.status = 'ACTIVE' "
               + "AND GETDATE() BETWEEN v.start_date AND v.end_date "
               + "AND v.min_order_value <= ? "
               + "AND (SELECT COUNT(*) FROM tbVoucherUsage vu WHERE vu.voucher_id = v.voucher_id) < v.max_usage_limit "
               + "AND (SELECT COUNT(*) FROM tbVoucherUsage vu2 WHERE vu2.voucher_id = v.voucher_id AND vu2.account_id = ?) < v.per_customer_limit";

    try (PreparedStatement stm = connection.prepareStatement(sql)) {
        stm.setInt(1, accountId);
        stm.setInt(2, voucherId);
        stm.setLong(3, orderSubtotal);
        stm.setInt(4, accountId);
        try (ResultSet rs = stm.executeQuery()) {
            if (rs.next()) {
                Long maxDiscountCap = null;
                long capRaw = rs.getLong("max_discount_cap");
                if (!rs.wasNull()) {
                    maxDiscountCap = capRaw;
                }
                return new VoucherDTO(
                        rs.getInt("voucher_id"),
                        rs.getString("code"),
                        rs.getString("discount_type"),
                        rs.getLong("discount_value"),
                        maxDiscountCap,
                        rs.getLong("min_order_value"),
                        rs.getTimestamp("start_date"),
                        rs.getTimestamp("end_date"),
                        rs.getInt("max_usage_limit"),
                        rs.getInt("per_customer_limit")
                );
            }
        }
        }catch(Exception ex){
        ex.printStackTrace();
    }
    return null;
}
    public List<Integer> getSavedVoucherIds(int accountId) {
    List<Integer> savedIds = new ArrayList<>();
    String sql = "SELECT voucher_id FROM tbSavedVoucher WHERE account_id = ?";
    
    try (PreparedStatement stm = connection.prepareStatement(sql)) {
        stm.setInt(1, accountId);
        try (ResultSet rs = stm.executeQuery()) {
            while (rs.next()) {
                savedIds.add(rs.getInt("voucher_id"));
            }
        }
    } catch (Exception ex) {
        ex.printStackTrace();
    }
    return savedIds;
    }
    public List<VoucherDTO> getAllVouchers() {
        List<VoucherDTO> list = new ArrayList<>();
        String sql = "SELECT voucher_id, code, discount_type, discount_value, max_discount_cap, "
                + "min_order_value, start_date, end_date, max_usage_limit, per_customer_limit, status "
                + "FROM tbVoucher ORDER BY start_date DESC";
        try (PreparedStatement ps = connection.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapRowWithStatus(rs));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public VoucherDTO getVoucherById(int voucherId) {
        VoucherDTO v = null;
        String sql = "SELECT voucher_id, code, discount_type, discount_value, max_discount_cap, "
                + "min_order_value, start_date, end_date, max_usage_limit, per_customer_limit, status "
                + "FROM tbVoucher WHERE voucher_id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, voucherId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    v = mapRowWithStatus(rs);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return v;
    }

    public boolean voucherCodeExists(String code) {
        String sql = "SELECT COUNT(*) FROM tbVoucher WHERE code = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, code);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean voucherCodeExistsExcept(String code, int voucherId) {
        String sql = "SELECT COUNT(*) FROM tbVoucher WHERE code = ? AND voucher_id <> ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, code);
            ps.setInt(2, voucherId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public int insertVoucher(VoucherDTO v) {
        String sql = "INSERT INTO tbVoucher "
                + "(code, discount_type, discount_value, max_discount_cap, min_order_value, "
                + "start_date, end_date, max_usage_limit, per_customer_limit, status) "
                + "VALUES (?,?,?,?,?,?,?,?,?,'ACTIVE')";
        try (PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, v.getCode());
            ps.setString(2, v.getDiscountType());
            ps.setLong(3, v.getDiscountValue());
            if (v.getMaxDiscountCap() != null) {
                ps.setLong(4, v.getMaxDiscountCap());
            } else {
                ps.setNull(4, java.sql.Types.BIGINT);
            }
            ps.setLong(5, v.getMinOrderValue());
            ps.setTimestamp(6, new Timestamp(v.getStartDate().getTime()));
            ps.setTimestamp(7, new Timestamp(v.getEndDate().getTime()));
            ps.setInt(8, v.getMaxUsageLimit());
            ps.setInt(9, v.getPerCustomerLimit());

            int affected = ps.executeUpdate();
            if (affected > 0) {
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) {
                        return keys.getInt(1);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return -1;
    }

    /**
     * Không cho sửa "code" — giữ nguyên tinh thần "không sửa SKU" áp dụng cho
     * Product
     */
    public boolean updateVoucher(VoucherDTO v) {
        String sql = "UPDATE tbVoucher SET discount_type=?, discount_value=?, max_discount_cap=?, "
                + "min_order_value=?, start_date=?, end_date=?, max_usage_limit=?, per_customer_limit=? "
                + "WHERE voucher_id=?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, v.getDiscountType());
            ps.setLong(2, v.getDiscountValue());
            if (v.getMaxDiscountCap() != null) {
                ps.setLong(3, v.getMaxDiscountCap());
            } else {
                ps.setNull(3, java.sql.Types.BIGINT);
            }
            ps.setLong(4, v.getMinOrderValue());
            ps.setTimestamp(5, new Timestamp(v.getStartDate().getTime()));
            ps.setTimestamp(6, new Timestamp(v.getEndDate().getTime()));
            ps.setInt(7, v.getMaxUsageLimit());
            ps.setInt(8, v.getPerCustomerLimit());
            ps.setInt(9, v.getVoucherId());
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean updateVoucherStatus(int voucherId, String status) {
        String sql = "UPDATE tbVoucher SET status=? WHERE voucher_id=?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setInt(2, voucherId);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private VoucherDTO mapRowWithStatus(ResultSet rs) throws Exception {
        Long maxDiscountCap = null;
        long capRaw = rs.getLong("max_discount_cap");
        if (!rs.wasNull()) {
            maxDiscountCap = capRaw;
        }
        VoucherDTO v = new VoucherDTO(
                rs.getInt("voucher_id"), rs.getString("code"), rs.getString("discount_type"),
                rs.getLong("discount_value"), maxDiscountCap, rs.getLong("min_order_value"),
                rs.getTimestamp("start_date"), rs.getTimestamp("end_date"),
                rs.getInt("max_usage_limit"), rs.getInt("per_customer_limit")
        );
        v.setStatus(rs.getString("status")); // MỚI: dùng setter vừa thêm
        return v;
    }

    /**
     * MỚI: tự động chuyển các voucher ACTIVE nhưng đã hết hạn
     */
    public void expireOverdueVouchers() {
        String sql = "UPDATE tbVoucher SET status = 'INACTIVE' "
                + "WHERE status = 'ACTIVE' AND end_date < GETDATE()";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
  
}
