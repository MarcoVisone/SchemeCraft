document.addEventListener("DOMContentLoaded", function () {
    const header = document.querySelector(".site-header");
    const SCROLL_THRESHOLD = 20;

    function updateHeaderState() {
        if (window.scrollY > SCROLL_THRESHOLD) {
            header.classList.add("site-header--scrolled");
        } else {
            header.classList.remove("site-header--scrolled");
        }
    }

    window.addEventListener("scroll", updateHeaderState);
    updateHeaderState(); // run once on load in case page is already scrolled
});