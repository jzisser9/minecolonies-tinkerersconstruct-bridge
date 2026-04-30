import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.api.colony.buildings.IBuilding;
import com.minecolonies.api.colony.requestsystem.requestable.deliveryman.Delivery;

public class test {
    public void foo(IBuilding blacksmith, ICitizenData citizen, Delivery delivery) {
        blacksmith.createRequest(citizen, delivery, true);
    }
}
