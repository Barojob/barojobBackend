package barojob.server.domain.sms.repository;

import barojob.server.domain.sms.entity.SmsEntity;
import org.springframework.data.repository.CrudRepository;

public interface SmsRepository extends CrudRepository<SmsEntity,String>,SmsCustomRepository{


}
