package com.ezweb.engine.rpc.balance.rule;

import com.ezweb.engine.rpc.balance.Server;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 轮叫调度
 *
 * @author zuodengpeng
 * @version 1.0.0
 * @date 2018/4/13
 */
public class RoundRobinBalancer<T extends Server> extends AbsLoadBalancer<T> {
	private AtomicInteger last = new AtomicInteger(0);

	public RoundRobinBalancer() {
	}

	@Override
	protected T chooseImpl(List<T> list) {
		int n = list.size();
		int j = this.last.getAndIncrement();
		T sel = null;
		for (int i = 0; i < n; ++i) {
			j = j % n;
			sel = list.get(j);
			if (sel.weight() > 0) {
				return sel;
			}
			sel = null;
			++j;
		}

		return sel;
	}
}
