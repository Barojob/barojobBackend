package barojob.server.domain.sms.repository;

import barojob.server.domain.sms.entity.SmsEntity;

public interface SmsCustomRepository  {


    void saveVerificationCode(SmsEntity smsEntity);
    String findByPhoneNumber(String phoneNumber);
    void deleteByPhoneNumber(String phoneNumber);
}