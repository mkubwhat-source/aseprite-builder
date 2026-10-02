package dev.hexnowloading.dungeonnowloading.capabilities.fabric;

import dev.hexnowloading.dungeonnowloading.item.client.DNLArmPose;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.ladysnake.cca.api.v3.entity.RespawnableComponent;

// CCA 8 (26.x): components serialize through ValueInput/ValueOutput.
public class DNLArmPoseCapabilityHandler implements DNLArmPoseComponent, RespawnableComponent<DNLArmPoseCapabilityHandler> {

    private DNLArmPose armPose = DNLArmPose.EMPTY; // Default Pose

    @Override
    public void setArmPose(DNLArmPose pose) {
        armPose = pose;
    }

    @Override
    public DNLArmPose getArmPose() {
        return armPose;
    }

    @Override
    public void readData(ValueInput input) {
        input.getStringOr("DNLArmPose", "").ifPresent(id -> this.armPose = DNLArmPose.fromId(id));
    }

    @Override
    public void writeData(ValueOutput output) {
        output.putString("DNLArmPose", armPose.getId());
    }

    @Override
    public void copyFrom(DNLArmPoseCapabilityHandler original, HolderLookup.Provider registries) {
        this.armPose = original.armPose;
    }
}
