package skjennum;

import jade.lang.acl.ACLMessage;

/**
 * Created by anon on 26.02.2015.
 */
public interface MessageListener {

  void newMessage(ACLMessage msg);
}
