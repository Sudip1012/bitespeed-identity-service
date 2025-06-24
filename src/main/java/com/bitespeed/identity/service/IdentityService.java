package com.bitespeed.identity.service;

import com.bitespeed.identity.dto.IdentifyRequest;
import com.bitespeed.identity.dto.IdentifyResponse;
import com.bitespeed.identity.entity.Contact;
import com.bitespeed.identity.repository.ContactRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class IdentityService {
    
    @Autowired
    private ContactRepository contactRepository;
    
    public IdentifyResponse identify(IdentifyRequest request) {
        String email = request.getEmail();
        String phoneNumber = request.getPhoneNumber();
        
        // Validate that at least one of email or phoneNumber is provided
        if ((email == null || email.trim().isEmpty()) && 
            (phoneNumber == null || phoneNumber.trim().isEmpty())) {
            throw new IllegalArgumentException("Either email or phoneNumber must be provided");
        }
        
        // Clean input data
        email = (email != null && !email.trim().isEmpty()) ? email.trim() : null;
        phoneNumber = (phoneNumber != null && !phoneNumber.trim().isEmpty()) ? phoneNumber.trim() : null;
        
        // Find existing contacts by email or phone
        List<Contact> existingContacts = contactRepository.findByEmailOrPhoneNumber(email, phoneNumber);
        
        if (existingContacts.isEmpty()) {
            // No existing contacts, create new primary contact
            return createNewPrimaryContact(email, phoneNumber);
        }
        
        // Check if exact match exists
        if (email != null && phoneNumber != null) {
            List<Contact> exactMatches = contactRepository.findByEmailAndPhoneNumber(email, phoneNumber);
            if (!exactMatches.isEmpty()) {
                // Exact match found, return consolidated contact
                Contact firstMatch = exactMatches.get(0);
                Contact primaryContact = findPrimaryContact(firstMatch);
                return buildConsolidatedResponse(primaryContact);
            }
        }
        
        // Group contacts by their primary contact
        Set<Contact> primaryContacts = new HashSet<>();
        for (Contact contact : existingContacts) {
            Contact primary = findPrimaryContact(contact);
            primaryContacts.add(primary);
        }
        
        if (primaryContacts.size() == 1) {
            // All existing contacts belong to the same primary contact
            Contact primaryContact = primaryContacts.iterator().next();
            
            // Check if we need to create a secondary contact
            if (shouldCreateSecondaryContact(primaryContact, email, phoneNumber)) {
                createSecondaryContact(primaryContact, email, phoneNumber);
            }
            
            return buildConsolidatedResponse(primaryContact);
        } else {
            // Multiple primary contacts found, need to link them
            return linkMultiplePrimaryContacts(primaryContacts, email, phoneNumber);
        }
    }
    
    private IdentifyResponse createNewPrimaryContact(String email, String phoneNumber) {
        Contact newContact = new Contact(phoneNumber, email, null, Contact.LinkPrecedence.PRIMARY);
        Contact savedContact = contactRepository.save(newContact);
        return buildConsolidatedResponse(savedContact);
    }
    
    private Contact findPrimaryContact(Contact contact) {
        if (contact.getLinkPrecedence() == Contact.LinkPrecedence.PRIMARY) {
            return contact;
        } else {
            return contactRepository.findById(contact.getLinkedId()).orElse(contact);
        }
    }
    
    private boolean shouldCreateSecondaryContact(Contact primaryContact, String email, String phoneNumber) {
        List<Contact> allLinkedContacts = contactRepository.findAllLinkedContacts(primaryContact.getId());
        
        // Check if the combination already exists
        for (Contact contact : allLinkedContacts) {
            if (Objects.equals(contact.getEmail(), email) && 
                Objects.equals(contact.getPhoneNumber(), phoneNumber)) {
                return false; // Exact match exists
            }
        }
        
        // Check if we have new information
        boolean hasNewEmail = email != null && allLinkedContacts.stream()
                .noneMatch(c -> Objects.equals(c.getEmail(), email));
        boolean hasNewPhone = phoneNumber != null && allLinkedContacts.stream()
                .noneMatch(c -> Objects.equals(c.getPhoneNumber(), phoneNumber));
        
        return hasNewEmail || hasNewPhone;
    }
    
    private void createSecondaryContact(Contact primaryContact, String email, String phoneNumber) {
        Contact secondaryContact = new Contact(phoneNumber, email, primaryContact.getId(), 
                                             Contact.LinkPrecedence.SECONDARY);
        contactRepository.save(secondaryContact);
    }
    
    private IdentifyResponse linkMultiplePrimaryContacts(Set<Contact> primaryContacts, 
                                                        String email, String phoneNumber) {
        // Find the oldest primary contact (by creation time)
        Contact oldestPrimary = primaryContacts.stream()
                .min(Comparator.comparing(Contact::getCreatedAt))
                .orElseThrow(() -> new RuntimeException("No primary contact found"));
        
        // Convert other primary contacts to secondary
        for (Contact primary : primaryContacts) {
            if (!primary.getId().equals(oldestPrimary.getId())) {
                // Update this primary to secondary
                primary.setLinkPrecedence(Contact.LinkPrecedence.SECONDARY);
                primary.setLinkedId(oldestPrimary.getId());
                contactRepository.save(primary);
                
                // Update all contacts linked to this primary to link to the oldest primary
                List<Contact> secondaryContacts = contactRepository.findAllLinkedContacts(primary.getId());
                for (Contact secondary : secondaryContacts) {
                    if (!secondary.getId().equals(primary.getId()) && 
                        !secondary.getId().equals(oldestPrimary.getId())) {
                        secondary.setLinkedId(oldestPrimary.getId());
                        contactRepository.save(secondary);
                    }
                }
            }
        }
        
        // Check if we need to create a new secondary contact
        if (shouldCreateSecondaryContact(oldestPrimary, email, phoneNumber)) {
            createSecondaryContact(oldestPrimary, email, phoneNumber);
        }
        
        return buildConsolidatedResponse(oldestPrimary);
    }
    
    private IdentifyResponse buildConsolidatedResponse(Contact primaryContact) {
        List<Contact> allLinkedContacts = contactRepository.findAllLinkedContacts(primaryContact.getId());
        
        // Sort by creation time to ensure consistent ordering
        allLinkedContacts.sort(Comparator.comparing(Contact::getCreatedAt));
        
        // Collect emails and phone numbers, maintaining order with primary first
        LinkedHashSet<String> emailSet = new LinkedHashSet<>();
        LinkedHashSet<String> phoneSet = new LinkedHashSet<>();
        List<Long> secondaryIds = new ArrayList<>();
        
        // Add primary contact's info first
        if (primaryContact.getEmail() != null) {
            emailSet.add(primaryContact.getEmail());
        }
        if (primaryContact.getPhoneNumber() != null) {
            phoneSet.add(primaryContact.getPhoneNumber());
        }
        
        // Add secondary contacts' info
        for (Contact contact : allLinkedContacts) {
            if (!contact.getId().equals(primaryContact.getId())) {
                if (contact.getEmail() != null) {
                    emailSet.add(contact.getEmail());
                }
                if (contact.getPhoneNumber() != null) {
                    phoneSet.add(contact.getPhoneNumber());
                }
                secondaryIds.add(contact.getId());
            }
        }
        
        IdentifyResponse.ContactInfo contactInfo = new IdentifyResponse.ContactInfo(
                primaryContact.getId(),
                new ArrayList<>(emailSet),
                new ArrayList<>(phoneSet),
                secondaryIds
        );
        
        return new IdentifyResponse(contactInfo);
    }
} 