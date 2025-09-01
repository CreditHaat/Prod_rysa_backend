package com.lsp.web.ONDCService;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.lsp.web.entity.*;
//import com.loan.predictor.entity.UserInfo;
import com.lsp.web.repository.*;

@Service
public class UserBureauDataService {

    @Autowired
    private UserBureauDataRepository userBureauDataRepository;

    public void saveOrUpdateBureauData(UserInfo user, String creditScore, String responseJson) {
        Optional<UserBureauData> bureauDataOpt = userBureauDataRepository.findByUserId(user.getId());

        UserBureauData bureauData = bureauDataOpt.orElse(new UserBureauData());
        bureauData.setUserId(user.getId());
        bureauData.setMobileNumber(user.getMobileNumber());
        bureauData.setCreditScore(creditScore);
        bureauData.setResponseContent(responseJson);

//        LocalDateTime registerTime = user.getRegisterTime().toInstant()
//            .atZone(ZoneId.systemDefault())
//            .toLocalDateTime();

//        bureauData.setRegisterTime(registerTime);

        userBureauDataRepository.save(bureauData);
    }

}