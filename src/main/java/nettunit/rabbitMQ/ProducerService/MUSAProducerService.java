package nettunit.rabbitMQ.ProducerService;

import RabbitMQ.JixelEvent;
import RabbitMQ.Producer.MUSAProducer;
import RabbitMQ.Producer.MUSARabbitMQProducer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import scala.collection.immutable.List;

@Service
public class MUSAProducerService implements MUSAProducer {
    private static Logger logger = LoggerFactory.getLogger(MUSAProducerService.class);

    private MUSARabbitMQProducer MUSA;

    @Autowired
    public MUSAProducerService() {
        MUSA = new MUSARabbitMQProducer();
    }

    @Override
    public String addRecipient(JixelEvent ev, List<Object> actors_id) {
        return MUSA.addRecipient(ev, actors_id);
    }

    @Override
    public String updateUrgencyLevel(JixelEvent ev, int incident_urgency_id) {
        return MUSA.updateUrgencyLevel(ev, incident_urgency_id);
    }

    @Override
    public String updateEventSeverity(JixelEvent ev, int incident_severity_id) {
        return MUSA.updateEventSeverity(ev, incident_severity_id);
    }

    @Override
    public String updateEventTypology(JixelEvent ev, int incident_type_id) {
        return MUSA.updateEventTypology(ev, incident_type_id);
    }

    @Override
    public String updateEventDescription(JixelEvent ev, String description) {
        return MUSA.updateEventDescription(ev, description);
    }

    @Override
    public String updateCommType(JixelEvent ev, int incident_msgtype_id) {
        return MUSA.updateCommType(ev, incident_msgtype_id);
    }
}
