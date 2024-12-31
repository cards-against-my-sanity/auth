package dev.jacobandersen.cams.auth;

import dev.jacobandersen.cams.auth.model.Role;
import dev.jacobandersen.cams.auth.model.User;
import dev.jacobandersen.cams.auth.repo.RoleRepository;
import dev.jacobandersen.cams.auth.repo.UserRepository;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.security.crypto.password.PasswordEncoder;

@SpringBootApplication
@EnableAspectJAutoProxy
@EnableAsync
public class AuthApplication {
    public static void main(String[] args) {
        SpringApplication.run(AuthApplication.class, args);
    }

    @EventListener(ContextRefreshedEvent.class)
    public void seedDatabase(final ContextRefreshedEvent event) {
        final ApplicationContext context = event.getApplicationContext();
        final RoleRepository roleRepository = context.getBean(RoleRepository.class);
        final PasswordEncoder encoder = context.getBean(PasswordEncoder.class);
        final UserRepository userRepository = context.getBean(UserRepository.class);

        if (roleRepository.count() == 0) {
            final Role admin = new Role();
            admin.setName("admin");
            roleRepository.save(admin);

            final Role moderator = new Role();
            moderator.setName("moderator");
            roleRepository.save(moderator);

            final Role user = new Role();
            user.setName("user");
            roleRepository.save(user);
        }

        if (userRepository.count() == 0) {
            final User user = new User();
            user.setEmail("root@example.org");
            user.setPassword(encoder.encode("i@mR00t!"));
            user.setNickname("root");
            user.setConfirmed(true);
            user.setRoles(roleRepository.findAll());

            userRepository.save(user);
        }
    }
}
