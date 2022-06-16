package nettunit.handler;

import org.flowable.engine.delegate.DelegateExecution;
import org.flowable.engine.delegate.JavaDelegate;

public class InternalTeamInformGestionnaireHandler implements JavaDelegate {

    @Override
    public void execute(DelegateExecution execution) {
        // MUSA -> Jixel (addrecipient)
        //addrecipient restituisce una stringa
        //se valore ok, allora non genero errore

        // if too much time passes, then throw error

        System.out.println("SENT TO MUSA");
    }
}

