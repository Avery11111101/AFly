package io.github.avery11111102.afly;

import org.bukkit.Location;
import org.bukkit.plugin.Plugin;

import java.lang.reflect.Method;
import java.util.UUID;

/**
 * 以反射呼叫 Residence 外掛的 API，避免在編譯期硬依賴特定 Residence 版本。
 * 對應方法（已對照 Residence 原始碼 / 執行期確認）：
 *   Residence(實例).getResidenceManager()  -> ResidenceManager
 *   ResidenceManager.getByLoc(Location)     -> ClaimedResidence(可能為 null)
 *   ClaimedResidence.getName()              -> String 領地名稱
 *   ClaimedResidence.getOwner()             -> String 擁有者名稱（向後相容）
 *   ClaimedResidence.getOwnerUUID()         -> UUID（新版 Residence）
 */
public class ResidenceHook {

    private final Plugin residence;
    private Method getResidenceManager;
    private Method getByLoc;
    private Method getName;
    private Method getOwner;
    private Method getOwnerUUID;
    private boolean ownerUUIDResolved;
    private boolean broken;

    public ResidenceHook(Plugin residence) {
        this.residence = residence;
        try {
            this.getResidenceManager = residence.getClass().getMethod("getResidenceManager");
        } catch (Throwable t) {
            this.broken = true;
        }
    }

    public boolean isBroken() {
        return broken || residence == null;
    }

    /** 回傳該座標所在的領地物件，荒野或不可用時回傳 null。 */
    public Object getResidence(Location loc) {
        if (isBroken()) return null;
        try {
            Object manager = getResidenceManager.invoke(residence);
            if (manager == null) return null;
            if (getByLoc == null) {
                getByLoc = manager.getClass().getMethod("getByLoc", Location.class);
            }
            return getByLoc.invoke(manager, loc);
        } catch (Throwable t) {
            return null;
        }
    }

    public String nameOf(Object claimed) {
        if (claimed == null) return null;
        try {
            if (getName == null) getName = claimed.getClass().getMethod("getName");
            Object r = getName.invoke(claimed);
            return r == null ? null : r.toString();
        } catch (Throwable t) {
            return null;
        }
    }

    public String ownerOf(Object claimed) {
        if (claimed == null) return null;
        try {
            if (getOwner == null) getOwner = claimed.getClass().getMethod("getOwner");
            Object r = getOwner.invoke(claimed);
            return r == null ? null : r.toString();
        } catch (Throwable t) {
            return null;
        }
    }

    /** 新版 Residence 可取得擁有者 UUID；舊版回 null（呼叫端會 fallback 用名稱）。 */
    public UUID ownerUUIDOf(Object claimed) {
        if (claimed == null) return null;
        try {
            if (!ownerUUIDResolved) {
                ownerUUIDResolved = true;
                try {
                    getOwnerUUID = claimed.getClass().getMethod("getOwnerUUID");
                } catch (NoSuchMethodException ignore) {
                    getOwnerUUID = null;
                }
            }
            if (getOwnerUUID == null) return null;
            Object r = getOwnerUUID.invoke(claimed);
            if (r instanceof UUID u) return u;
            if (r != null) return UUID.fromString(r.toString());
            return null;
        } catch (Throwable t) {
            return null;
        }
    }
}
