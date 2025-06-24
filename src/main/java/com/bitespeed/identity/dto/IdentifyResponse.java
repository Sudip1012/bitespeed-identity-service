package com.bitespeed.identity.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class IdentifyResponse {
    
    @JsonProperty("contact")
    private ContactInfo contact;
    
    // Constructors
    public IdentifyResponse() {}
    
    public IdentifyResponse(ContactInfo contact) {
        this.contact = contact;
    }
    
    // Getters and Setters
    public ContactInfo getContact() {
        return contact;
    }
    
    public void setContact(ContactInfo contact) {
        this.contact = contact;
    }
    
    public static class ContactInfo {
        
        @JsonProperty("primaryContatctId") // Note: keeping the typo as per requirement
        private Long primaryContactId;
        
        @JsonProperty("emails")
        private List<String> emails;
        
        @JsonProperty("phoneNumbers") 
        private List<String> phoneNumbers;
        
        @JsonProperty("secondaryContactIds")
        private List<Long> secondaryContactIds;
        
        // Constructors
        public ContactInfo() {}
        
        public ContactInfo(Long primaryContactId, List<String> emails, 
                          List<String> phoneNumbers, List<Long> secondaryContactIds) {
            this.primaryContactId = primaryContactId;
            this.emails = emails;
            this.phoneNumbers = phoneNumbers;
            this.secondaryContactIds = secondaryContactIds;
        }
        
        // Getters and Setters
        public Long getPrimaryContactId() {
            return primaryContactId;
        }
        
        public void setPrimaryContactId(Long primaryContactId) {
            this.primaryContactId = primaryContactId;
        }
        
        public List<String> getEmails() {
            return emails;
        }
        
        public void setEmails(List<String> emails) {
            this.emails = emails;
        }
        
        public List<String> getPhoneNumbers() {
            return phoneNumbers;
        }
        
        public void setPhoneNumbers(List<String> phoneNumbers) {
            this.phoneNumbers = phoneNumbers;
        }
        
        public List<Long> getSecondaryContactIds() {
            return secondaryContactIds;
        }
        
        public void setSecondaryContactIds(List<Long> secondaryContactIds) {
            this.secondaryContactIds = secondaryContactIds;
        }
        
        @Override
        public String toString() {
            return "ContactInfo{" +
                    "primaryContactId=" + primaryContactId +
                    ", emails=" + emails +
                    ", phoneNumbers=" + phoneNumbers +
                    ", secondaryContactIds=" + secondaryContactIds +
                    '}';
        }
    }
    
    @Override
    public String toString() {
        return "IdentifyResponse{" +
                "contact=" + contact +
                '}';
    }
} 