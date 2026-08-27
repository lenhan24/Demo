/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package model;

/**
 *
 * @author GiGaByte
 */
public class Address  {
    private int addressId;
    private int accountId;
    private String receiverName;
    private String receiverPhone;
    private String fullAddress;
    private boolean isDefault;

    // Constructor không tham số
    public Address() {
    }

    // Constructor đầy đủ tham số
    public Address(int addressId, int accountId, String receiverName, String receiverPhone, String fullAddress, boolean isDefault) {
        this.addressId = addressId;
        this.accountId = accountId;
        this.receiverName = receiverName;
        this.receiverPhone = receiverPhone;
        this.fullAddress = fullAddress;
        this.isDefault = isDefault;
    }

    // Getters and Setters
    public int getAddressId() { return addressId; }
    public void setAddressId(int addressId) { this.addressId = addressId; }

    public int getAccountId() { return accountId; }
    public void setAccountId(int accountId) { this.accountId = accountId; }

    public String getReceiverName() { return receiverName; }
    public void setReceiverName(String receiverName) { this.receiverName = receiverName; }

    public String getReceiverPhone() { return receiverPhone; }
    public void setReceiverPhone(String receiverPhone) { this.receiverPhone = receiverPhone; }

    public String getFullAddress() { return fullAddress; }
    public void setFullAddress(String fullAddress) { this.fullAddress = fullAddress; }

    public boolean getIsDefault() { return isDefault; }
    public void setDefault(boolean isDefault) { this.isDefault = isDefault; }

    @Override
    public String toString() {
        return "Address{" + "addressId=" + addressId + ", accountId=" + accountId + ", receiverName=" + receiverName + ", receiverPhone=" + receiverPhone + ", fullAddress=" + fullAddress + ", isDefault=" + isDefault + '}';
    }
    
    
}