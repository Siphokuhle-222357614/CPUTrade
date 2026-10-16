import { afterEach, describe, expect, it } from "vitest";
import { h, nextTick } from "vue";
import { mount } from "@vue/test-utils";
import Modal from "./Modal.vue";

describe("accessible modal", () => {
  afterEach(() => {
    document.body.innerHTML = "";
  });

  it("labels the dialog, traps keyboard focus, closes on Escape, and restores focus", async () => {
    const trigger = document.createElement("button");
    document.body.appendChild(trigger);
    trigger.focus();

    const wrapper = mount(Modal, {
      attachTo: document.body,
      props: { open: false, title: "Suspend account" },
      slots: {
        default: () => [h("button", "First action"), h("button", "Last action")],
      },
    });
    await wrapper.setProps({ open: true });
    await nextTick();

    const dialog = document.body.querySelector('[role="dialog"]');
    expect(dialog.getAttribute("aria-labelledby")).toBe("modal-title");
    expect(dialog.getAttribute("aria-modal")).toBe("true");
    expect(document.activeElement).toBe(dialog);

    const buttons = dialog.querySelectorAll("button");
    buttons[2].focus();
    dialog.dispatchEvent(new KeyboardEvent("keydown", { key: "Tab", bubbles: true }));
    expect(document.activeElement).toBe(buttons[0]);

    buttons[0].focus();
    dialog.dispatchEvent(new KeyboardEvent("keydown", { key: "Tab", shiftKey: true, bubbles: true }));
    expect(document.activeElement).toBe(buttons[2]);

    dialog.dispatchEvent(new KeyboardEvent("keydown", { key: "Escape", bubbles: true }));
    expect(wrapper.emitted("close")).toHaveLength(1);

    await wrapper.setProps({ open: false });
    await nextTick();
    expect(document.activeElement).toBe(trigger);
    wrapper.unmount();
  });
});
