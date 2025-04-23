package barojob.server.domain.sms.service;

import barojob.server.domain.sms.entity.SmsEntity;
import barojob.server.domain.sms.repository.SmsRepository;
import net.nurigo.sdk.NurigoApp;
import net.nurigo.sdk.message.exception.NurigoMessageNotReceivedException;
import net.nurigo.sdk.message.model.Message;
import net.nurigo.sdk.message.service.DefaultMessageService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Random;

@Service
public class SmsService {
    private final SmsRepository smsRepository;
    @Value("${coolsms.api.key}")
    private String apiKey;
    @Value("${coolsms.api.secret}")
    private String apiSecret;


    public SmsService(SmsRepository smsRepository) {
        this.smsRepository = smsRepository;
    }


    private String generateVerificationCode(){
        Random random=new Random();
        return String.format("%06d",random.nextInt(1000000));
    }

    public void saveVerificationCode(String phoneNumber){
        String verificationCode = generateVerificationCode();
        SmsEntity newSms = new SmsEntity(phoneNumber, verificationCode);

        System.out.println("verificationCode :"+verificationCode);
        smsRepository.deleteByPhoneNumber(phoneNumber);
        smsRepository.saveVerificationCode(newSms);

        sendSms(phoneNumber,verificationCode);
    }

    private void sendSms(String phoneNumber, String verificationCode){
        DefaultMessageService messageService = NurigoApp.INSTANCE.initialize(apiKey,apiSecret,"https://api.coolsms.co.kr");
        Message message=new Message();
        message.setFrom("01028609697");
        message.setTo(phoneNumber);
        message.setText("[바로잡]인증번호 "+verificationCode+"를 입력해주세요.");

        try {
            // send 메소드로 ArrayList<Message> 객체를 넣어도 동작합니다!
            messageService.send(message);
        } catch (NurigoMessageNotReceivedException exception) {
            // 발송에 실패한 메시지 목록을 확인할 수 있습니다!
            System.out.println("send fail! : 1");
            System.out.println(exception.getFailedMessageList());
            System.out.println(exception.getMessage());
        } catch (Exception exception) {
            System.out.println("send fail! : 2");
            System.out.println(exception.getMessage());
        }
    }

    public boolean verifyCode(String phoneNumber, String verificationCode){
        String storedCode=smsRepository.findByPhoneNumber(phoneNumber);
        System.out.println("phoneNumber: "+phoneNumber+" storedCode: "+storedCode);
        System.out.println("input verificationCode: "+verificationCode);
        return storedCode!=null && storedCode.equals(verificationCode);
    }
}