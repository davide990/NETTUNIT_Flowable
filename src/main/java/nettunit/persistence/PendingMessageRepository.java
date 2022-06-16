package nettunit.persistence;

import org.springframework.data.repository.CrudRepository;

public interface PendingMessageRepository extends CrudRepository<PendingMessage, Long> {

    PendingMessage findByTaskID(String taskID);

    PendingMessage findByCommType(String commType);

}
