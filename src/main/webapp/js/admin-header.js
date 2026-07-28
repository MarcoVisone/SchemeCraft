document.addEventListener("DOMContentLoaded", function () {
    const toggleButton = document.getElementById("adminHeaderToggle");
    const menu = document.getElementById("adminHeaderMenu");

    if (!toggleButton || !menu) {
        return;
    }

    toggleButton.addEventListener("click", function () {
        const isOpen = menu.classList.toggle("admin-header__menu--open");
        toggleButton.setAttribute("aria-expanded", isOpen ? "true" : "false");
        toggleButton.classList.toggle("admin-header__toggle--active", isOpen);
    });


    menu.querySelectorAll("a").forEach(function (link) {
        link.addEventListener("click", function () {
            menu.classList.remove("admin-header__menu--open");
            toggleButton.setAttribute("aria-expanded", "false");
            toggleButton.classList.remove("admin-header__toggle--active");
        });
    });
});