package nettunit.taskService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class TaskListenerService {
    List<Integer> myList;

    @Autowired
    public TaskListenerService() {
        myList = new ArrayList<>();
    }

    //TODO to implement
    public void myFun() {
        //myList.add(33);
        //System.out.println(myList.stream().map(x -> Integer.toString(x)).collect(Collectors.joining(", ")).toString());
    }

}