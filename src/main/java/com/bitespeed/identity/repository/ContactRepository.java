package com.bitespeed.identity.repository;

import com.bitespeed.identity.entity.Contact;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ContactRepository extends JpaRepository<Contact, Long> {
    
    /**
     * Find contacts by email or phone number
     */
    @Query("SELECT c FROM Contact c WHERE " +
           "(c.email = :email AND :email IS NOT NULL) OR " +
           "(c.phoneNumber = :phoneNumber AND :phoneNumber IS NOT NULL)")
    List<Contact> findByEmailOrPhoneNumber(@Param("email") String email, 
                                          @Param("phoneNumber") String phoneNumber);
    
    /**
     * Find contacts by email
     */
    List<Contact> findByEmail(String email);
    
    /**
     * Find contacts by phone number
     */
    List<Contact> findByPhoneNumber(String phoneNumber);
    
    /**
     * Find all contacts linked to a primary contact (including the primary itself)
     */
    @Query("SELECT c FROM Contact c WHERE c.id = :primaryId OR c.linkedId = :primaryId ORDER BY c.createdAt ASC")
    List<Contact> findAllLinkedContacts(@Param("primaryId") Long primaryId);
    
    /**
     * Find primary contact by ID or find the primary of a secondary contact
     */
    @Query("SELECT c FROM Contact c WHERE " +
           "(c.id = :contactId AND c.linkPrecedence = 'PRIMARY') OR " +
           "(c.id = (SELECT sc.linkedId FROM Contact sc WHERE sc.id = :contactId))")
    Contact findPrimaryContact(@Param("contactId") Long contactId);
    
    /**
     * Find contacts that exactly match both email and phone
     */
    @Query("SELECT c FROM Contact c WHERE c.email = :email AND c.phoneNumber = :phoneNumber")
    List<Contact> findByEmailAndPhoneNumber(@Param("email") String email, 
                                           @Param("phoneNumber") String phoneNumber);
} 