package patchi.distomc.item;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;
import patchi.distomc.SurveyReading;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.particle.DustParticleEffect;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.world.World;
import net.minecraft.util.math.Vec3d;

import org.joml.Vector3f;

public class DistoItem extends Item {

    private static float laserDensity = 0.1f;
    private static float epsilon = 0.01f;

    private enum distoMode {
        RANGEFIND("range finder"),
        MEASURE("measure") {
            @Override
            protected distoMode next() {
                return values()[0];
            }
        };

        private final String label;

        distoMode(String label) {
            this.label = label;
        }

        distoMode next() {
            return values()[ordinal() + 1];
        }

        @Override
        public String toString() {
            return label;
        }
    }

    public DistoItem(Settings settings) {
        super(settings);
    }

    public static void setupKeyBind() {


    }

    private distoMode mode = distoMode.MEASURE;

    private Vec3d activePoint = null;
    private Vec3d lastPoint = null;
    private int nextCounter = 0;

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {

        if (context.getWorld().isClient()) {
            distoBehaviour(context.getWorld(), context.getPlayer());
        }

        return ActionResult.SUCCESS;
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {

        if (world.isClient()) {
            distoBehaviour(world, player);
        }

        return TypedActionResult.success(player.getStackInHand(hand), world.isClient());

    }

    private void distoBehaviour(World world, PlayerEntity player) {

        MinecraftClient client = MinecraftClient.getInstance();
        HitResult hit = client.cameraEntity.raycast(1000f, 1f, true);

        if (mode == distoMode.RANGEFIND) {

            switch (hit.getType()) {
                case MISS:
                    break;
                default:
                    Vec3d startPoint = client.cameraEntity.getEyePos();
                    Vec3d ray = hit.getPos().subtract(startPoint);

                    player.sendMessage(Text.literal(new SurveyReading(getHeading(ray), getInclination(ray), ray.length()).printShortString()),true);
                    drawLaser(world, startPoint, ray);
                    break;

            }

        } else if (mode == distoMode.MEASURE) {

            if (activePoint == null) {
                activePoint = hit.getPos();
                lastPoint = activePoint;
            } else {

                Vec3d targetPoint = hit.getPos();

                if (targetPoint.distanceTo(lastPoint) < epsilon) {

                    nextCounter++;

                    if (nextCounter >= 2) {
                        nextCounter = 0;
                        Vec3d ray = targetPoint.subtract(activePoint);

                        SurveyReading S = new SurveyReading(getHeading(ray), getInclination(ray), ray.length());

                        player.sendMessage(Text.literal(S.printShortString()), true);

                        drawLaser(world, activePoint, ray);
                        activePoint = targetPoint;
                    }

                } else {
                    nextCounter = 0;
                    Vec3d ray = targetPoint.subtract(activePoint);
                    drawLaser(world, activePoint, ray, new Vector3f(0, 0, 1));
                }
                lastPoint = targetPoint;
            }

        }

    }

    public void changeMode() {
        MinecraftClient client = MinecraftClient.getInstance();

        if (mode == distoMode.MEASURE) {
            activePoint = null;
        }

        mode = mode.next();
        client.player.sendMessage(Text.literal("Changed to " + mode + " mode."), true);
    }

    private void drawLaser(World world, Vec3d startPoint, Vec3d ray) {
        drawLaser(world, startPoint, ray, new Vector3f(1, 0, 0));
    }

    private void drawLaser(World world, Vec3d startPoint, Vec3d ray, Vector3f colour) {

        int laserPoints = (int) Math.ceil(ray.length() / laserDensity);
        Vec3d increment = ray.normalize().multiply(laserDensity);

        for (int i = 1; i <= laserPoints; i++) {
            Vec3d nextPoint = startPoint.add(increment.multiply(i));
            world.addParticle(new DustParticleEffect(colour, 1), nextPoint.x, nextPoint.y, nextPoint.z, 0, 0, 0);
        }

    }

    private double getHeading(Vec3d V) {
        return 180 - (Math.toDegrees(Math.atan2(V.x, V.z)));
    }

    private double getInclination(Vec3d V) {
        return Math.toDegrees(Math.asin(V.y / V.length()));
    }

}
