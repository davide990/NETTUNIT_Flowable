package nettunit.StateMachine;

public interface ProcessState {

    void act();

    void next(CurrentProcessContext pkg);

    void prev(CurrentProcessContext pkg);

}
