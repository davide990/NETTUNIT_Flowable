package nettunit.rabbitMQ.ProducerService;

import RabbitMQ.JixelEvent;
import RabbitMQ.Producer.MUSAProducer;
import RabbitMQ.Producer.MUSARabbitMQProducer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class MUSAProducerService implements MUSAProducer {
    private static Logger logger = LoggerFactory.getLogger(MUSAProducerService.class);

    private MUSARabbitMQProducer MUSA;

    @Autowired
    public MUSAProducerService() {
        MUSA = new MUSARabbitMQProducer();
    }

    @Override
    public String notifyEvent(JixelEvent event) {
        return MUSA.notifyEvent(event);
    }

    @Override
    public String addRecipient(JixelEvent ev, String recipient) {
        return MUSA.addRecipient(ev, recipient);
    }

    @Override
    public String updateUrgencyLevel(JixelEvent ev, String level) {
        return MUSA.updateUrgencyLevel(ev, level);
    }

    @Override
    public String updateEventSeverity(JixelEvent ev, String severity) {
        return MUSA.updateEventSeverity(ev, severity);
    }

    @Override
    public String updateEventTypology(JixelEvent ev, String typology) {
        return MUSA.updateEventTypology(ev, typology);
    }

    @Override
    public String updateEventDescription(JixelEvent ev, String description) {
        return MUSA.updateEventDescription(ev, description);
    }

    @Override
    public String updateCommType(JixelEvent ev, String commType) {
        return MUSA.updateCommType(ev, commType);
    }
}
