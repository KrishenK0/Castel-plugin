package fr.krishenk.castel.commands.admin.debugging;

import fr.krishenk.castel.commands.CastelCommand;
import fr.krishenk.castel.commands.CastelParentCommand;
import fr.krishenk.castel.commands.CommandContext;
import fr.krishenk.castel.commands.CommandTabContext;
import fr.krishenk.castel.data.Pair;
import fr.krishenk.castel.libs.xseries.ReflectionUtils;
import fr.krishenk.castel.libs.xseries.particles.ParticleDisplay;
import fr.krishenk.castel.libs.xseries.particles.XParticle;
import fr.krishenk.castel.locale.MessageHandler;
import fr.krishenk.castel.utils.LocationUtils;
import fr.krishenk.castel.utils.time.TimeFormatter;
import org.apache.commons.lang.Validate;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Zombie;
import org.bukkit.scheduler.BukkitRunnable;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

public class CommandAdminEntity extends CastelCommand {

    public CommandAdminEntity(CastelParentCommand parent) {
        super("entity", parent);
    }

    public static String details(Entity entity) {
        Validate.notNull(entity);
        List<Pair<String, Object>> details = new ArrayList<>();
        List<String> flags = new ArrayList<>();
        if (entity.isInvulnerable()) flags.add("Invulnerable");
        if (entity.isSilent()) flags.add("Silent");

        if (entity instanceof LivingEntity) {
            details.add(Pair.of("No Damage Ticks", ((LivingEntity) entity).getNoDamageTicks()));
            if (((LivingEntity) entity).isCollidable()) flags.add("Collidable");
            if (((LivingEntity) entity).isInvisible()) flags.add("Invisible");

            if (ReflectionUtils.supports(13) && entity instanceof Zombie && ((Zombie) entity).isConverting()) {
                details.add(Pair.of("Convertion Time", TimeFormatter.of(((Zombie) entity).getConversionTime())));
            }
        }

        StringBuilder detailStr = new StringBuilder();
        if (!flags.isEmpty()) {
            detailStr.append("\n   &8| &9");
            detailStr.append(String.join("&7 &9", flags));
        }

        Iterator<Pair<String, Object>> it = details.iterator();

        while (true) {
            String key;
            Object value;

            do {
                if (!it.hasNext()) return detailStr.toString();

                Pair<String, Object> detail = it.next();
                key = detail.getKey();
                value = detail.getValue();
            } while (value instanceof Number && ((Number) value).doubleValue() == 0.0);

            detailStr.append("\n   &8| &2").append(key).append("&7: &9").append(value);
        }
    }

    @Override
    public void execute(CommandContext context) {
        if (!context.assertPlayer()) {
            final double radius = context.assertArgs(1) ? context.getDouble(0) : 10.0;
            int stay = context.assertArgs(2) ? context.getInt(1) : 1;
            final boolean showDetails = context.assertArgs(3) && Objects.equals(context.arg(2), "true");
            if (radius <= 1.0) MessageHandler.sendPluginMessage(context.getSender(), "&cNegative radius&8:  &e" + radius);
            else {
                if (stay <= 0) MessageHandler.sendPluginMessage(context.getSender(), "&cNegative stay number&8: &e" + stay);
                else {
                    final AtomicInteger total = new AtomicInteger();
                    final Player player = context.senderAsPlayer();
                    (new BukkitRunnable() {
                        private Integer times = stay;
                        private boolean looping;

                        public Integer getTimes() {
                            return times;
                        }

                        public void setTimes(Integer times) {
                            this.times = times;
                        }

                        public boolean isLooping() {
                            return looping;
                        }

                        public void setLooping(boolean looping) {
                            this.looping = looping;
                        }

                        @Override
                        public void run() {
                            for (Entity entity : player.getNearbyEntities(radius, radius, radius)) {
                                if (!this.looping) {
                                    Location loc = entity.getLocation();
                                    StringBuilder builder = (new StringBuilder()).append("&8 &2").append(entity.getType()).append("(").append(entity.getCustomName()).append("&2) &6hover:{").append(LocationUtils.toReadableLoc(loc)).append(";Click to teleport;/minecraft:tp ").append(loc.getX()).append(' ').append(loc.getY()).append(' ').append(loc.getZ()).append('}');
                                    String details = showDetails ? details(entity) : "";

                                    MessageHandler.sendMessage(player, builder.append(details).toString());
                                }

                                ParticleDisplay.of(Particle.FLAME).withLocation(entity.getLocation()).withCount(20).offset(0.3).spawn();
                                XParticle.line(player.getLocation(), entity.getLocation(), 0.5, ParticleDisplay.of(Particle.SPELL_WITCH));
                                if (!this.looping) total.incrementAndGet();
                            }

                            if (!this.looping) {
                                this.looping = true;
                                MessageHandler.sendPluginMessage(player, "&2Found a total of &6" + total + " &2entities within &6" + radius + " &2block radius.");
                            }

                            if (--this.times <= 0) this.cancel();
                        }
                    }).runTaskTimer(plugin, 0L, 5L);
                }
            }
        }
    }

    @Override
    public @NonNull List<String> tabComplete(CommandTabContext context) {
        if (context.isAtArg(0)) return tabComplete("[radius]");
        if (context.isAtArg(1)) return tabComplete("[stay]");
        if (context.isAtArg(2)) return tabComplete("[show details?]");
        return emptyTab();
    }
}
