package nettunit.rabbitMQ.ProducerService;

import RabbitMQ.JixelEvent;
import RabbitMQ.JixelEventReport;
import RabbitMQ.JixelEventSummary;
import RabbitMQ.JixelEventUpdate;
import RabbitMQ.Producer.JixelProducer;
import RabbitMQ.Producer.JixelRabbitMQProducer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class JixelProducerService implements JixelProducer {
    private static Logger logger = LoggerFactory.getLogger(MUSAProducerService.class);

    private JixelRabbitMQProducer Jixel;

    @Autowired
    public JixelProducerService() {
        Jixel = new JixelRabbitMQProducer();
    }

    @Override
    public String notifyEvent(JixelEvent event) {
        return Jixel.notifyEvent(event);
    }

    @Override
    public String notifyEventSummary(JixelEventSummary event) {
        return Jixel.notifyEventSummary(event);
    }

    @Override
    public String updateEvent(JixelEventUpdate update) {
        return Jixel.updateEvent(update);
    }

    @Override
    public String notifyReport(JixelEventReport report) {
        return Jixel.notifyReport(report);
    }
}

