package com.lsp.web.repository;

import org.springframework.stereotype.Repository;

import com.lsp.web.entity.Master_City_State;

import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;

@Repository
public class MasterCityStateCustomRepository {

    @PersistenceContext
    private EntityManager entityManager;

    public Master_City_State findByPincodeAndPartner(Integer pincode, String partner) {
        String sql = "SELECT m FROM Master_City_State m WHERE m.pincode = :pincode AND m." + partner + " = 'Y'";
        
        try {
            return entityManager.createQuery(sql, Master_City_State.class)
                    .setParameter("pincode", pincode)
                    .getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }
}
